package com.ufps.signaco.core.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ufps.signaco.feature.ajustes.presentation.AjustesScreen
import com.ufps.signaco.feature.deletreo.presentation.DeletreoScreen
import com.ufps.signaco.feature.ajustes.presentation.TamanoYContrasteScreen
import com.ufps.signaco.feature.diccionario.presentation.DiccionarioScreen
import com.ufps.signaco.feature.diccionario.presentation.teoria.TeoriaSenaScreen
import com.ufps.signaco.feature.lecciones.presentation.catalogo.CatalogoLeccionesScreen
import com.ufps.signaco.feature.lecciones.presentation.detalle.DetalleLeccionScreen
import com.ufps.signaco.feature.lecciones.presentation.primeruso.ConoceTuManoScreen
import com.ufps.signaco.feature.lecciones.presentation.sena.DetalleSenaScreen
import com.ufps.signaco.feature.practica.presentation.PracticaScreen
import com.ufps.signaco.feature.progreso.presentation.ProgresoScreen
import com.ufps.signaco.feature.quiz.presentation.QuizCamaraScreen
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
                    navController.navigate(Rutas.Practica(leccionId, senaInicial = senaId, soloEstaSena = true))
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
                onAbrirDeletreo = { navController.navigate(Rutas.Deletreo) },
            )
        }

        composable<Rutas.Deletreo> {
            DeletreoScreen(onVolver = { navController.navigateUp() })
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
                // «Empezar práctica» recorre la lección: primero la indicación de la primera letra.
                onPracticar = { leccionId, primeraSenaId ->
                    navController.navigate(Rutas.DetalleSena(leccionId, primeraSenaId, enRecorrido = true))
                },
                onHacerQuiz = { leccionId -> navController.navigate(Rutas.Quiz(leccionId)) },
            )
        }

        composable<Rutas.DetalleSena> {
            DetalleSenaScreen(
                onCerrar = { navController.navigateUp() },
                // Tocando una letra de la lista se practica solo esa; con «Empezar práctica» la
                // cámara sigue luego con las demás letras de la lección.
                onProbarConCamara = { leccionId, senaId, enRecorrido ->
                    navController.navigate(
                        Rutas.Practica(leccionId, senaInicial = senaId, soloEstaSena = !enRecorrido),
                    )
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
                onVolverALaLeccion = { navController.volverALaLeccion() },
                // La indicación de la siguiente letra reemplaza a la indicación y la cámara de la
                // anterior: así «atrás» siempre lleva a la lección y la pila no crece letra a letra.
                onSiguienteSena = { leccionId, senaId ->
                    navController.navigate(Rutas.DetalleSena(leccionId, senaId, enRecorrido = true)) {
                        popUpTo(Rutas.DetalleLeccion(leccionId))
                    }
                },
            )
        }

        // Parte 1: reconocimiento con imagen. Al terminar sigue la parte 2, con cámara.
        composable<Rutas.Quiz> {
            QuizScreen(
                onVolver = { navController.navigateUp() },
                onQuizTerminado = { leccionId, resultado ->
                    navController.navigate(
                        Rutas.QuizCamara(leccionId, aciertosImagen = resultado.aciertos, totalImagen = resultado.total),
                    ) {
                        popUpTo<Rutas.Quiz> { inclusive = true }
                    }
                },
            )
        }

        // Parte 2 (RF15): hacer las letras frente a la cámara. Su resultado aprueba la lección.
        composable<Rutas.QuizCamara> { entrada ->
            val ruta = entrada.toRoute<Rutas.QuizCamara>()
            QuizCamaraScreen(
                onVolver = { navController.navigateUp() },
                onQuizTerminado = { leccionId, resultado ->
                    navController.navigate(
                        Rutas.ResultadoQuiz(
                            leccionId = leccionId,
                            aciertos = resultado.aciertos,
                            total = resultado.total,
                            senasDelQuiz = resultado.respuestas.map { it.senaId },
                            aciertosPorPregunta = resultado.respuestas.map { it.acerto },
                            aciertosImagen = ruta.aciertosImagen,
                            totalImagen = ruta.totalImagen,
                        ),
                    ) {
                        popUpTo<Rutas.QuizCamara> { inclusive = true }
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

/**
 * Regresa a la lista de letras de la lección, cerrando la indicación y la cámara de la letra
 * suelta. Si se llegó desde el diccionario no hay lección en la pila: se vuelve a la pantalla
 * anterior, que es la teoría de la seña.
 */
private fun NavHostController.volverALaLeccion() {
    if (!popBackStack<Rutas.DetalleLeccion>(inclusive = false)) navigateUp()
}
