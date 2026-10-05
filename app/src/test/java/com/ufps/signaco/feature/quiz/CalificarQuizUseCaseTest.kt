package com.ufps.signaco.feature.quiz

import com.ufps.signaco.feature.quiz.domain.model.PreguntaQuiz
import com.ufps.signaco.feature.quiz.domain.usecase.CalificarQuizUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalificarQuizUseCaseTest {

    private val caso = CalificarQuizUseCase()

    private val preguntas = listOf(
        pregunta("A"),
        pregunta("B"),
        pregunta("C"),
        pregunta("D"),
        pregunta("E"),
    )

    @Test
    fun `cuenta bien los aciertos`() {
        val resultado = caso(
            preguntas,
            respuestas = mapOf("A" to "A", "B" to "X", "C" to "C", "D" to "D", "E" to "E"),
        )

        assertEquals(4, resultado.aciertos)
        assertEquals(5, resultado.total)
    }

    @Test
    fun `las preguntas sin responder cuentan como error`() {
        val resultado = caso(preguntas, respuestas = mapOf("A" to "A"))

        assertEquals(1, resultado.aciertos)
        assertFalse(resultado.aprobado)
    }

    @Test
    fun `se aprueba justo en el umbral`() {
        val resultado = caso(
            preguntas,
            respuestas = mapOf("A" to "A", "B" to "B", "C" to "C", "D" to "D", "E" to "X"),
        )

        assertEquals(0.8f, resultado.porcentaje, 0.0001f)
        assertTrue(resultado.aprobado)
    }

    @Test
    fun `no se aprueba por debajo del umbral`() {
        val resultado = caso(
            preguntas,
            respuestas = mapOf("A" to "A", "B" to "B", "C" to "C", "D" to "X", "E" to "X"),
        )

        assertEquals(0.6f, resultado.porcentaje, 0.0001f)
        assertFalse(resultado.aprobado)
    }

    @Test
    fun `un quiz vacio no divide por cero ni se da por aprobado`() {
        val resultado = caso(emptyList(), respuestas = emptyMap())

        assertEquals(0f, resultado.porcentaje, 0.0001f)
        assertFalse(resultado.aprobado)
    }

    private fun pregunta(letra: String) = PreguntaQuiz(
        senaId = letra,
        enunciado = "Descripción de $letra",
        letraCorrecta = letra,
        opciones = listOf(letra, "X", "Y", "Z"),
    )

    @Test
    fun `devuelve cada respuesta calificada en el orden de las preguntas`() {
        val resultado = caso(
            preguntas,
            respuestas = mapOf("A" to "A", "B" to "X", "C" to "C"),
        )

        assertEquals(listOf("A", "B", "C", "D", "E"), resultado.respuestas.map { it.senaId })
        assertEquals(listOf(true, false, true, false, false), resultado.respuestas.map { it.acerto })
    }
}
