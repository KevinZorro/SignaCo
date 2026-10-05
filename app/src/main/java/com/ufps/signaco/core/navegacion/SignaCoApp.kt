package com.ufps.signaco.core.navegacion

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.componentes.BarraDeNavegacion
import com.ufps.signaco.core.ui.componentes.ElementoDeNavegacion
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import kotlin.reflect.KClass

/**
 * Armazón de la app: tema según los ajustes de apariencia, barra inferior con los tres
 * destinos principales y el grafo de navegación. La barra desaparece en las pantallas de
 * paso (detalle, práctica, quiz, ajustes) salvo la teoría de una seña, que es parte del
 * diccionario.
 */
@Composable
fun SignaCoApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: SignaCoAppViewModel = hiltViewModel(),
) {
    val preferencias by viewModel.preferencias.collectAsStateWithLifecycle()
    val leidas = preferencias

    SignaCoTheme(
        altoContraste = leidas?.altoContraste ?: false,
        escalaDeTexto = leidas?.tamanoDeTexto?.escala ?: 1f,
    ) {
        // Mientras se leen los ajustes se pinta solo el fondo: así no aparece un instante el
        // catálogo antes del primer uso.
        if (leidas == null) {
            Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            return@SignaCoTheme
        }
        // El destino inicial se decide una vez; marcar el primer uso no debe rehacer el grafo.
        val destinoInicial: Any = remember { if (leidas.primerUsoCompletado) Rutas.Lecciones else Rutas.ConoceTuMano }
        Armazon(navController, destinoInicial, modifier)
    }
}

@Composable
private fun Armazon(navController: NavHostController, destinoInicial: Any, modifier: Modifier) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val mostrarBarraInferior = DESTINOS_PRINCIPALES.any { destino ->
        destino.clasesDeRuta.any { destinoActual.estaEn(it) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (mostrarBarraInferior) {
                BarraDeNavegacion(
                    elementos = DESTINOS_PRINCIPALES.map { destino ->
                        ElementoDeNavegacion(
                            etiqueta = stringResource(destino.etiqueta),
                            icono = destino.icono,
                            seleccionado = destino.clasesDeRuta.any { destinoActual.estaEn(it) },
                            onClick = { navController.irADestinoPrincipal(destino.ruta) },
                        )
                    },
                )
            }
        },
    ) { relleno ->
        SignaCoNavHost(
            navController = navController,
            destinoInicial = destinoInicial,
            modifier = Modifier
                .padding(relleno)
                .consumeWindowInsets(relleno),
        )
    }
}

internal fun NavHostController.irADestinoPrincipal(ruta: Any) {
    navigate(ruta) {
        // Se guarda el estado del destino que se deja para poder volver a él tal cual. La raíz
        // es Lecciones y no el inicio del grafo: tras el primer uso, ese inicio ya no está en la
        // pila.
        popUpTo<Rutas.Lecciones> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavDestination?.estaEn(claseDeRuta: KClass<*>): Boolean =
    this?.hierarchy?.any { it.hasRoute(claseDeRuta) } == true

/** @param clasesDeRuta las rutas donde la pestaña se ve seleccionada y la barra visible. */
private data class DestinoPrincipal(
    val ruta: Any,
    val clasesDeRuta: List<KClass<*>>,
    @StringRes val etiqueta: Int,
    @DrawableRes val icono: Int,
)

private val DESTINOS_PRINCIPALES = listOf(
    DestinoPrincipal(
        ruta = Rutas.Lecciones,
        clasesDeRuta = listOf(Rutas.Lecciones::class),
        etiqueta = R.string.nav_lecciones,
        icono = R.drawable.ic_menu_book,
    ),
    DestinoPrincipal(
        ruta = Rutas.Diccionario,
        clasesDeRuta = listOf(Rutas.Diccionario::class, Rutas.TeoriaSena::class),
        etiqueta = R.string.nav_diccionario,
        icono = R.drawable.ic_book_2,
    ),
    DestinoPrincipal(
        ruta = Rutas.Progreso,
        clasesDeRuta = listOf(Rutas.Progreso::class),
        etiqueta = R.string.nav_progreso,
        icono = R.drawable.ic_grid_view,
    ),
)
