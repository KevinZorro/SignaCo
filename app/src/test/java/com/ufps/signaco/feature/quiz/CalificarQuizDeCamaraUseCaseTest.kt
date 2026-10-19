package com.ufps.signaco.feature.quiz

import com.ufps.signaco.feature.quiz.domain.model.RespuestaCalificada
import com.ufps.signaco.feature.quiz.domain.usecase.CalificarQuizDeCamaraUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** La parte de cámara decide si la lección se aprueba: 70 %, es decir 4 de 5. */
class CalificarQuizDeCamaraUseCaseTest {

    private val calificar = CalificarQuizDeCamaraUseCase()

    private fun respuestas(vararg aciertos: Boolean) =
        aciertos.mapIndexed { i, acerto -> RespuestaCalificada("S$i", acerto) }

    @Test
    fun `cuatro de cinco aprueba`() {
        val resultado = calificar(respuestas(true, true, false, true, true), totalDePreguntas = 5)

        assertEquals(4, resultado.aciertos)
        assertTrue(resultado.aprobado)
    }

    @Test
    fun `tres de cinco no aprueba`() {
        assertFalse(calificar(respuestas(true, false, true, false, true), totalDePreguntas = 5).aprobado)
    }

    @Test
    fun `las preguntas sin contestar cuentan como error`() {
        val resultado = calificar(respuestas(true, true, true), totalDePreguntas = 5)

        assertEquals(5, resultado.total)
        assertFalse(resultado.aprobado)
    }

    @Test
    fun `conserva las respuestas en orden para el desglose`() {
        val dadas = respuestas(false, true)

        assertEquals(dadas, calificar(dadas, totalDePreguntas = 2).respuestas)
    }

    @Test
    fun `un quiz vacio no se aprueba`() {
        assertFalse(calificar(emptyList(), totalDePreguntas = 0).aprobado)
    }
}
