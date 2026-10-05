package com.ufps.signaco.core.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ufps.signaco.feature.ajustes.presentation.AjustesScreen
import com.ufps.signaco.feature.ajustes.presentation.TamanoYContrasteScreen
import com.ufps.signaco.feature.diccionario.presentation.DiccionarioScreen
import com.ufps.signaco.feature.diccionario.presentation.teoria.TeoriaSenaScreen
import com.ufps.signaco.feature.lecciones.presentation.catalogo.CatalogoLeccionesScreen
import com.ufps.signaco.feature.lecciones.presentation.detalle.DetalleLeccionScreen
import com.ufps.signaco.feature.lecciones.presentation.primeruso.ConoceTuManoScreen
import com.ufps.signaco.feature.lecciones.presentation.sena.DetalleSenaScreen
import com.ufps.signaco.feature.practica.presentation.PracticaScreen
import com.ufps.signaco.feature.progreso.presentation.ProgresoScreen
import com.ufps.signaco.feature.quiz.presentation.QuizScreen
import com.ufps.signaco.feature.quiz.presentation.ResultadoQuizScreen

/**
 * Grafo de navegación. Las pantallas no conocen al NavController: reciben lambdas y avisan
 * qué pasó. Así se pueden previsualizar y probar sueltas.
 */
@Composable
fun SignaCoNavHost(
    navController: NavHostController,
    destinoInicial: Any,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = destinoInicial,
        modifier = modifier,
    ) {
        composable<Rutas.ConoceTuMano> {
            ConoceTuManoScreen(
                onContinuar = {
                    navController.navigate(Rutas.Lecciones) {
                        popUpTo<Rutas.ConoceTuMano> { inclusive = true }
                    }
                },
            )
        }

        composable<Rutas.Lecciones> {
            CatalogoLeccionesScreen(
                onLeccionSeleccionada = { leccionId ->
                    navController.navigate(Rutas.DetalleLeccion(leccionId))
                },
            )
        }

        composable<Rutas.Diccionario> {
            DiccionarioScreen(
                onSenaSeleccionada = { senaId -> navController.navigate(Rutas.TeoriaSena(senaId)) },
            )
        }

        composable<Rutas.TeoriaSena> {
            TeoriaSenaScreen(
                onCerrar = { navController.navigateUp() },
                onIniciarIntento = { leccionId, senaId ->
                    navController.navigate(Rutas.Practica(leccionId, senaInicial = senaId))
                },
            )
        }

        composable<Rutas.Progreso> {
            ProgresoScreen(onAbrirAjustes = { navController.navigate(Rutas.Ajustes) })
        }

        composable<Rutas.Ajustes> {
            AjustesScreen(
                onVolver = { navController.navigateUp() },
                onAbrirTamanoYContraste = { navController.navigate(Rutas.TamanoYContraste) },
            )
        }

        composable<Rutas.TamanoYContraste> {
            TamanoYContrasteScreen(onVolver = { navController.navigateUp() })
        }

        composable<Rutas.DetalleLeccion> {
            DetalleLeccionScreen(
                onVolver = { navController.navigateUp() },
                onSenaSeleccionada = { leccionId, senaId ->
                    navController.navigate(Rutas.DetalleSena(leccionId, senaId))
                },
                onPracticar = { leccionId -> navController.navigate(Rutas.Practica(leccionId)) },
                onHacerQuiz = { leccionId -> navController.navigate(Rutas.Quiz(leccionId)) },
            )
        }

        composable<Rutas.DetalleSena> {
            DetalleSenaScreen(
                onCerrar = { navController.navigateUp() },
                onProbarConCamara = { leccionId, senaId ->
                    navController.navigate(Rutas.Practica(leccionId, senaInicial = senaId))
                },
            )
        }

        composable<Rutas.Practica> {
            PracticaScreen(
                onVolver = { navController.navigateUp() },
                onTerminar = { leccionId ->
                    navController.navigate(Rutas.Quiz(leccionId)) {
                        popUpTo(Rutas.DetalleLeccion(leccionId))
                    }
                },
            )
        }

        composable<Rutas.Quiz> {
            QuizScreen(
                onVolver = { navController.navigateUp() },
                onQuizTerminado = { leccionId, resultado ->
                    navController.navigate(
                        Rutas.ResultadoQuiz(
                            leccionId = leccionId,
                            aciertos = resultado.aciertos,
                            total = resultado.total,
                            senasDelQuiz = resultado.respuestas.map { it.senaId },
                            senasFalladas = resultado.respuestas.filterNot { it.acerto }.map { it.senaId },
                        ),
                    ) {
                        popUpTo<Rutas.Quiz> { inclusive = true }
                    }
                },
            )
        }

        composable<Rutas.ResultadoQuiz> { entrada ->
            val ruta = entrada.toRoute<Rutas.ResultadoQuiz>()
            ResultadoQuizScreen(
                onRepetirQuiz = {
                    navController.navigate(Rutas.Quiz(ruta.leccionId)) {
                        popUpTo<Rutas.ResultadoQuiz> { inclusive = true }
                    }
                },
                onRepasar = { leccionId, senaId ->
                    navController.navigate(Rutas.DetalleSena(leccionId, senaId))
                },
                onVerProgreso = { navController.irADestinoPrincipal(Rutas.Progreso) },
                onCerrar = { navController.popBackStack(Rutas.Lecciones, inclusive = false) },
            )
        }
    }
}
