package com.ufps.signaco.feature.quiz.domain.model

/**
 * Una pregunta del quiz con cámara (RF15): la persona ve solo la letra y tiene que hacerla.
 *
 * @param senaId la etiqueta que devuelve el clasificador ("A".."Z"); es la que se evalúa.
 * @param letra lo que se le muestra a la persona.
 */
data class PreguntaDeCamara(
    val senaId: String,
    val letra: String,
)
