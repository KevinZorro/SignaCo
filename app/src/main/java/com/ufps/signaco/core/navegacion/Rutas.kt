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
    data object ConoceTuMano

    @Serializable
    data object Ajustes

    @Serializable
    data object TamanoYContraste

    @Serializable
    data object Deletreo

    @Serializable
    data class DetalleLeccion(val leccionId: String)

    /**
     * La indicación de una seña antes de la cámara.
     *
     * @param enRecorrido true si se llegó con «Empezar práctica»: la cámara sigue luego con las
     *        demás letras de la lección. False si se tocó la letra en la lista: se practica sola.
     */
    @Serializable
    data class DetalleSena(val leccionId: String, val senaId: String, val enRecorrido: Boolean = false)

    @Serializable
    data class TeoriaSena(val senaId: String)

    /**
     * @param senaInicial la seña con la que arranca; null empieza por la primera.
     * @param soloEstaSena true si se entró tocando una letra de la lista: se practica solo esa
     *        y al acertar se vuelve a la lección, sin seguir con las demás.
     */
    @Serializable
    data class Practica(
        val leccionId: String,
        val senaInicial: String? = null,
        val soloEstaSena: Boolean = false,
    )

    @Serializable
    data class Quiz(val leccionId: String)

    /**
     * Parte 2 del quiz (RF15), con lo que dejó la parte 1 para mostrarlo en el resultado.
     */
    @Serializable
    data class QuizCamara(val leccionId: String, val aciertosImagen: Int = 0, val totalImagen: Int = 0)

    /**
     * @param aciertos y [total] son de la parte de cámara, que es la que aprueba la lección.
     * @param senasDelQuiz una por pregunta de cámara, en orden y con repeticiones.
     * @param aciertosPorPregunta uno por pregunta, en el mismo orden.
     * @param aciertosImagen y [totalImagen] son de la parte 1; total 0 si no la hubo.
     */
    @Serializable
    data class ResultadoQuiz(
        val leccionId: String,
        val aciertos: Int,
        val total: Int,
        val senasDelQuiz: List<String> = emptyList(),
        val aciertosPorPregunta: List<Boolean> = emptyList(),
        val aciertosImagen: Int = 0,
        val totalImagen: Int = 0,
    )
}
