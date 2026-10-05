package com.ufps.signaco.feature.quiz.domain.model

/** Si el usuario acertó la pregunta de una seña. */
data class RespuestaCalificada(
    val senaId: String,
    val acerto: Boolean,
)

/**
 * Calificación de un quiz terminado.
 *
 * @param respuestas una por pregunta, en el orden en que se hicieron.
 */
data class ResultadoQuiz(
    val aciertos: Int,
    val total: Int,
    val respuestas: List<RespuestaCalificada> = emptyList(),
) {
    val porcentaje: Float
        get() = if (total == 0) 0f else aciertos.toFloat() / total

    val aprobado: Boolean
        get() = total > 0 && porcentaje >= PORCENTAJE_PARA_APROBAR

    companion object {
        const val PORCENTAJE_PARA_APROBAR = 0.7f
    }
}
