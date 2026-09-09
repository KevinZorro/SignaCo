package com.ufps.signaco.feature.quiz.domain.model

/** Calificación de un quiz terminado. */
data class ResultadoQuiz(
    val aciertos: Int,
    val total: Int,
) {
    val porcentaje: Float
        get() = if (total == 0) 0f else aciertos.toFloat() / total

    val aprobado: Boolean
        get() = total > 0 && porcentaje >= PORCENTAJE_PARA_APROBAR

    companion object {
        const val PORCENTAJE_PARA_APROBAR = 0.7f
    }
}
