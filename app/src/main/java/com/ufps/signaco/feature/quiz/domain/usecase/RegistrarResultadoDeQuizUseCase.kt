package com.ufps.signaco.feature.quiz.domain.usecase

import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import com.ufps.signaco.feature.progreso.domain.usecase.ActualizarRachaUseCase
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz
import javax.inject.Inject

/**
 * Guarda el desenlace del quiz. La lección solo se marca como completada si el quiz se
 * aprobó; responder el quiz cuenta como actividad del día en cualquier caso.
 */
class RegistrarResultadoDeQuizUseCase @Inject constructor(
    private val progresoRepository: ProgresoRepository,
    private val actualizarRacha: ActualizarRachaUseCase,
) {
    suspend operator fun invoke(leccionId: String, resultado: ResultadoQuiz) {
        if (resultado.aprobado) {
            progresoRepository.marcarLeccionCompletada(
                leccionId = leccionId,
                aciertosQuiz = resultado.aciertos,
                totalQuiz = resultado.total,
            )
        }
        actualizarRacha()
    }
}
