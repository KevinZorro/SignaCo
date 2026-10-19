package com.ufps.signaco.feature.quiz.domain.model

import com.ufps.signaco.core.senas.domain.model.ReferenciaVisual

/**
 * Pregunta de reconocimiento: se muestra la imagen de referencia de una seña y el usuario
 * elige a qué letra corresponde.
 *
 * @param referencia la imagen de la seña. Solo se pregunta por señas que tienen una.
 * @param opciones incluye siempre a [letraCorrecta], ya mezcladas.
 */
data class PreguntaQuiz(
    val senaId: String,
    val referencia: ReferenciaVisual,
    val letraCorrecta: String,
    val opciones: List<String>,
)
