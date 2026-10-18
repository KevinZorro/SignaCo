package com.ufps.signaco.feature.quiz.domain.usecase

import com.ufps.signaco.feature.quiz.domain.model.RespuestaCalificada
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz
import javax.inject.Inject

/**
 * Califica la parte de cámara. Las preguntas que no se llegaron a contestar cuentan como error:
 * [totalDePreguntas] es lo que se pidió, no lo que se respondió.
 *
 * @param respuestas una por pregunta contestada, en orden.
 */
class CalificarQuizDeCamaraUseCase @Inject constructor() {

    operator fun invoke(respuestas: List<RespuestaCalificada>, totalDePreguntas: Int): ResultadoQuiz =
        ResultadoQuiz(
            aciertos = respuestas.count { it.acerto },
            total = maxOf(totalDePreguntas, respuestas.size),
            respuestas = respuestas,
        )
}
