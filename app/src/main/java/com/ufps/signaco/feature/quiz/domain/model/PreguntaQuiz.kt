package com.ufps.signaco.feature.quiz.domain.model

/**
 * Pregunta de opción múltiple: se describe una seña y el usuario elige a qué letra corresponde.
 *
 * @param opciones incluye siempre a [letraCorrecta], ya mezcladas.
 */
data class PreguntaQuiz(
    val senaId: String,
    val enunciado: String,
    val letraCorrecta: String,
    val opciones: List<String>,
)
