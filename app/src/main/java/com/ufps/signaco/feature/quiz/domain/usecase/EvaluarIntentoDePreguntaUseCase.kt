package com.ufps.signaco.feature.quiz.domain.usecase

import com.ufps.signaco.feature.quiz.domain.model.DecisionDeIntento
import javax.inject.Inject

/**
 * Decide qué pasa con una pregunta del quiz con cámara tras un intento confirmado: si acertó,
 * la pregunta se gana; si falló, tiene otra oportunidad hasta gastar [INTENTOS_POR_PREGUNTA].
 *
 * Cálculo puro: el intento ya quedó registrado en el progreso por el reconocimiento.
 *
 * @param intentosUsados cuántos intentos confirmados lleva en esta pregunta, contando este.
 */
class EvaluarIntentoDePreguntaUseCase @Inject constructor() {

    operator fun invoke(acerto: Boolean, intentosUsados: Int): DecisionDeIntento = when {
        acerto -> DecisionDeIntento.Acertada
        intentosUsados >= INTENTOS_POR_PREGUNTA -> DecisionDeIntento.Fallada
        else -> DecisionDeIntento.OtraOportunidad(INTENTOS_POR_PREGUNTA - intentosUsados)
    }

    companion object {
        const val INTENTOS_POR_PREGUNTA = 3
    }
}
