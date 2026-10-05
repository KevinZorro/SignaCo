package com.ufps.signaco.core.navegacion

import kotlinx.serialization.Serializable

/**
 * Destinos de la app. Son rutas con tipo (Navigation Compose 2.8+): los argumentos viajan
 * como propiedades y el compilador avisa si falta alguno, en vez de armar cadenas a mano.
 */
object Rutas {

    @Serializable
    data object Lecciones

    @Serializable
    data object Diccionario

    @Serializable
    data object Progreso

    @Serializable
    data class DetalleLeccion(val leccionId: String)

    /** @param senaInicial la seña con la que arranca; null empieza por la primera. */
    @Serializable
    data class Practica(val leccionId: String, val senaInicial: String? = null)

    @Serializable
    data class Quiz(val leccionId: String)

    @Serializable
    data class ResultadoQuiz(
        val leccionId: String,
        val aciertos: Int,
        val total: Int,
    )
}
