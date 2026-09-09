package com.ufps.signaco.core.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ufps.signaco.feature.diccionario.presentation.DiccionarioScreen
import com.ufps.signaco.feature.lecciones.presentation.catalogo.CatalogoLeccionesScreen
import com.ufps.signaco.feature.lecciones.presentation.detalle.DetalleLeccionScreen
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
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.Lecciones,
        modifier = modifier,
    ) {
        composable<Rutas.Lecciones> {
            CatalogoLeccionesScreen(
                onLeccionSeleccionada = { leccionId ->
                    navController.navigate(Rutas.DetalleLeccion(leccionId))
                },
            )
        }

        composable<Rutas.Diccionario> {
            DiccionarioScreen()
        }

        composable<Rutas.Progreso> {
            ProgresoScreen()
        }

        composable<Rutas.DetalleLeccion> {
            DetalleLeccionScreen(
                onVolver = { navController.navigateUp() },
                onPracticar = { leccionId -> navController.navigate(Rutas.Practica(leccionId)) },
                onHacerQuiz = { leccionId -> navController.navigate(Rutas.Quiz(leccionId)) },
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
                onQuizTerminado = { leccionId, aciertos, total ->
                    navController.navigate(Rutas.ResultadoQuiz(leccionId, aciertos, total)) {
                        popUpTo(Rutas.Quiz(leccionId)) { inclusive = true }
                    }
                },
            )
        }

        composable<Rutas.ResultadoQuiz> { entrada ->
            val ruta = entrada.toRoute<Rutas.ResultadoQuiz>()
            ResultadoQuizScreen(
                aciertos = ruta.aciertos,
                total = ruta.total,
                onRepetirQuiz = {
                    navController.navigate(Rutas.Quiz(ruta.leccionId)) {
                        popUpTo(Rutas.ResultadoQuiz(ruta.leccionId, ruta.aciertos, ruta.total)) {
                            inclusive = true
                        }
                    }
                },
                onVolverALecciones = {
                    navController.popBackStack(Rutas.Lecciones, inclusive = false)
                },
            )
        }
    }
}
