package com.ufps.signaco.feature.practica.domain.model

/**
 * Lo que le pasó al usuario en un intento, ya comparado contra la seña que estaba practicando.
 *
 * Es distinto de ResultadoClasificacion: ese dice qué vio el modelo, este dice si el usuario
 * acertó. La comparación es del dominio, no de la pantalla.
 */
sealed interface ResultadoIntento {

    data class Acierto(val letra: String, val confianza: Float) : ResultadoIntento

    /** El modelo reconoció una letra, pero no era la que se estaba practicando. */
    data class OtraLetra(val letraDetectada: String, val confianza: Float) : ResultadoIntento

    /** Había mano en cuadro, pero la pose no corresponde a ninguna letra. */
    data object NoReconocida : ResultadoIntento

    /** No se detectó mano: no cuenta como intento. */
    data object SinMano : ResultadoIntento
}
