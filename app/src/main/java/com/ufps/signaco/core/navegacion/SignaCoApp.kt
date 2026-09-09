package com.ufps.signaco.core.navegacion

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ufps.signaco.R
import kotlin.reflect.KClass

/**
 * Armazón de la app: barra inferior con los tres destinos principales y el grafo de navegación.
 * La barra desaparece en las pantallas de detalle, práctica y quiz, que son de paso.
 */
@Composable
fun SignaCoApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val mostrarBarraInferior = DESTINOS_PRINCIPALES.any { destinoActual.estaEn(it.claseDeRuta) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (mostrarBarraInferior) {
                NavigationBar {
                    DESTINOS_PRINCIPALES.forEach { destino ->
                        val seleccionado = destinoActual.estaEn(destino.claseDeRuta)
                        NavigationBarItem(
                            selected = seleccionado,
                            onClick = { navController.irADestinoPrincipal(destino.ruta) },
                            icon = {
                                Icon(
                                    imageVector = destino.icono,
                                    // La etiqueta de texto siempre está visible debajo del
                                    // icono, así que describirla otra vez sería redundante
                                    // para TalkBack.
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(destino.etiqueta)) },
                        )
                    }
                }
            }
        },
    ) { relleno ->
        SignaCoNavHost(
            navController = navController,
            modifier = Modifier
                .padding(relleno)
                .consumeWindowInsets(relleno),
        )
    }
}

private fun NavHostController.irADestinoPrincipal(ruta: Any) {
    navigate(ruta) {
        // Se guarda el estado del destino que se deja para poder volver a él tal cual.
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavDestination?.estaEn(claseDeRuta: KClass<*>): Boolean =
    this?.hierarchy?.any { it.hasRoute(claseDeRuta) } == true

private data class DestinoPrincipal(
    val ruta: Any,
    val claseDeRuta: KClass<*>,
    @StringRes val etiqueta: Int,
    val icono: ImageVector,
)

private val DESTINOS_PRINCIPALES = listOf(
    DestinoPrincipal(
        ruta = Rutas.Lecciones,
        claseDeRuta = Rutas.Lecciones::class,
        etiqueta = R.string.nav_lecciones,
        icono = Icons.Default.List,
    ),
    DestinoPrincipal(
        ruta = Rutas.Diccionario,
        claseDeRuta = Rutas.Diccionario::class,
        etiqueta = R.string.nav_diccionario,
        icono = Icons.Default.Search,
    ),
    DestinoPrincipal(
        ruta = Rutas.Progreso,
        claseDeRuta = Rutas.Progreso::class,
        etiqueta = R.string.nav_progreso,
        icono = Icons.Default.Star,
    ),
)
