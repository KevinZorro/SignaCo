package com.ufps.signaco.feature.quiz

import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.fakes.FakeRachaRepository
import com.ufps.signaco.fakes.RelojFijo
import com.ufps.signaco.feature.progreso.domain.usecase.ActualizarRachaUseCase
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz
import com.ufps.signaco.feature.quiz.domain.usecase.RegistrarResultadoDeQuizUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrarResultadoDeQuizUseCaseTest {

    private val progreso = FakeProgresoRepository()
    private val racha = FakeRachaRepository()

    private val caso = RegistrarResultadoDeQuizUseCase(
        progresoRepository = progreso,
        actualizarRacha = ActualizarRachaUseCase(racha, RelojFijo(dia = 20)),
    )

    @Test
    fun `aprobar el quiz marca la leccion como completada`() = runTest {
        caso("leccion_1", ResultadoQuiz(aciertos = 5, total = 5))

        assertEquals(setOf("leccion_1"), progreso.completadas.value)
    }

    @Test
    fun `reprobar el quiz no completa la leccion`() = runTest {
        caso("leccion_1", ResultadoQuiz(aciertos = 1, total = 5))

        assertTrue(progreso.completadas.value.isEmpty())
    }

    @Test
    fun `responder el quiz cuenta como actividad del dia aunque se repruebe`() = runTest {
        caso("leccion_1", ResultadoQuiz(aciertos = 0, total = 5))

        assertEquals(1, racha.racha.value.diasConsecutivos)
        assertEquals(20L, racha.racha.value.ultimoDiaActivo)
    }
}
