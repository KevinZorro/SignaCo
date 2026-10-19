package com.ufps.signaco.feature.quiz

import com.ufps.signaco.feature.quiz.domain.model.DecisionDeIntento
import com.ufps.signaco.feature.quiz.domain.usecase.EvaluarIntentoDePreguntaUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

/** Hasta tres intentos por pregunta del quiz con cámara. */
class EvaluarIntentoDePreguntaUseCaseTest {

    private val evaluar = EvaluarIntentoDePreguntaUseCase()

    @Test
    fun `acertar gana la pregunta en cualquier intento`() {
        (1..3).forEach { intento ->
            assertEquals(DecisionDeIntento.Acertada, evaluar(acerto = true, intentosUsados = intento))
        }
    }

    @Test
    fun `fallar el primer intento deja dos oportunidades`() {
        assertEquals(DecisionDeIntento.OtraOportunidad(2), evaluar(acerto = false, intentosUsados = 1))
    }

    @Test
    fun `fallar el segundo intento deja una oportunidad`() {
        assertEquals(DecisionDeIntento.OtraOportunidad(1), evaluar(acerto = false, intentosUsados = 2))
    }

    @Test
    fun `fallar el tercer intento pierde la pregunta`() {
        assertEquals(DecisionDeIntento.Fallada, evaluar(acerto = false, intentosUsados = 3))
    }

    @Test
    fun `son tres intentos por pregunta`() {
        assertEquals(3, EvaluarIntentoDePreguntaUseCase.INTENTOS_POR_PREGUNTA)
    }
}
