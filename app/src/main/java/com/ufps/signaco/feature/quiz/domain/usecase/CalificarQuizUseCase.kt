package com.ufps.signaco.feature.quiz.domain.usecase

import com.ufps.signaco.feature.quiz.domain.model.PreguntaQuiz
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
    ): ResultadoQuiz = ResultadoQuiz(
        aciertos = preguntas.count { respuestas[it.senaId] == it.letraCorrecta },
        total = preguntas.size,
    )
}
