package com.ufps.signaco.feature.quiz.domain.usecase

import com.ufps.signaco.feature.quiz.domain.model.PreguntaQuiz
import com.ufps.signaco.feature.quiz.domain.model.RespuestaCalificada
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz
import javax.inject.Inject

/**
 * Cuenta aciertos. Cálculo puro, sin acceso a datos: las preguntas sin responder cuentan
 * como error.
 *
 * @param respuestas identificador de seña a letra elegida por el usuario.
 */
class CalificarQuizUseCase @Inject constructor() {

    operator fun invoke(
        preguntas: List<PreguntaQuiz>,
        respuestas: Map<String, String>,
    ): ResultadoQuiz {
        val calificadas = preguntas.map { pregunta ->
            RespuestaCalificada(
                senaId = pregunta.senaId,
                acerto = respuestas[pregunta.senaId] == pregunta.letraCorrecta,
            )
        }
        return ResultadoQuiz(
            aciertos = calificadas.count { it.acerto },
            total = preguntas.size,
            respuestas = calificadas,
        )
    }
}
