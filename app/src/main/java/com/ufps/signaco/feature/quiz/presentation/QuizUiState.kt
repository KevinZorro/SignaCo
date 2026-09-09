package com.ufps.signaco.feature.quiz.presentation

import com.ufps.signaco.feature.quiz.domain.model.PreguntaQuiz

data class QuizUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val preguntas: List<PreguntaQuiz> = emptyList(),
    val indiceActual: Int = 0,
    /** Identificador de seña a letra elegida. */
    val respuestas: Map<String, String> = emptyMap(),
) {
    val preguntaActual: PreguntaQuiz? get() = preguntas.getOrNull(indiceActual)

    val numeroDePregunta: Int get() = indiceActual + 1

    val total: Int get() = preguntas.size

    val esLaUltima: Boolean get() = preguntas.isNotEmpty() && indiceActual >= preguntas.lastIndex

    val letraSeleccionada: String? get() = preguntaActual?.let { respuestas[it.senaId] }

    val sinPreguntas: Boolean get() = !cargando && !hayError && preguntas.isEmpty()
}
