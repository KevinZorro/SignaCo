package com.ufps.signaco.feature.quiz

import com.ufps.signaco.fakes.FakeLeccionRepository
import com.ufps.signaco.fakes.FakeSenaRepository
import com.ufps.signaco.fakes.leccionDePrueba
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.quiz.domain.usecase.GenerarQuizUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GenerarQuizUseCaseTest {

    private val vocabulario = listOf(
        senaDePrueba("A", orden = 1),
        senaDePrueba("B", orden = 2),
        senaDePrueba("C", orden = 3),
        senaDePrueba("D", orden = 4),
        senaDePrueba("E", orden = 5),
        senaDePrueba("F", orden = 6),
        senaDePrueba("NN", orden = 7, disponible = false),
    )

    private val leccion = leccionDePrueba(
        id = "leccion_1",
        senas = vocabulario.take(5) + vocabulario.last(),
    )

    private fun crearCaso() = GenerarQuizUseCase(
        leccionRepository = FakeLeccionRepository(listOf(leccion)),
        senaRepository = FakeSenaRepository(vocabulario),
    )

    @Test
    fun `genera tantas preguntas como se piden`() = runTest {
        val preguntas = crearCaso()("leccion_1", numeroDePreguntas = 3, random = Random(1))

        assertEquals(3, preguntas.size)
    }

    @Test
    fun `nunca pide mas preguntas que senas practicables tenga la leccion`() = runTest {
        val preguntas = crearCaso()("leccion_1", numeroDePreguntas = 20, random = Random(1))

        // La lección tiene 6 señas, pero una está fuera del MVP.
        assertEquals(5, preguntas.size)
    }

    @Test
    fun `las senas fuera del MVP no se preguntan ni aparecen como distractores`() = runTest {
        val preguntas = crearCaso()("leccion_1", numeroDePreguntas = 5, random = Random(7))

        assertTrue(preguntas.none { it.senaId == "NN" })
        assertTrue(preguntas.none { pregunta -> pregunta.opciones.any { it == "NN" } })
    }

    @Test
    fun `cada pregunta trae cuatro opciones distintas e incluye la correcta`() = runTest {
        val preguntas = crearCaso()("leccion_1", numeroDePreguntas = 5, random = Random(3))

        preguntas.forEach { pregunta ->
            assertEquals(
                GenerarQuizUseCase.OPCIONES_POR_PREGUNTA,
                pregunta.opciones.distinct().size,
            )
            assertTrue(pregunta.letraCorrecta in pregunta.opciones)
        }
    }

    @Test
    fun `con la misma semilla se genera exactamente el mismo quiz`() = runTest {
        val primero = crearCaso()("leccion_1", numeroDePreguntas = 4, random = Random(42))
        val segundo = crearCaso()("leccion_1", numeroDePreguntas = 4, random = Random(42))

        assertEquals(primero, segundo)
    }

    @Test
    fun `una leccion que no existe no genera preguntas`() = runTest {
        val preguntas = crearCaso()("no_existe", numeroDePreguntas = 5, random = Random(1))

        assertTrue(preguntas.isEmpty())
    }
}
