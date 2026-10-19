package com.ufps.signaco.feature.quiz

import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.fakes.FakeLeccionRepository
import com.ufps.signaco.fakes.leccionDePrueba
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.quiz.domain.model.PreguntaDeCamara
import com.ufps.signaco.feature.quiz.domain.usecase.GenerarQuizDeCamaraUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** RF15: las letras de la lección que hay que hacer frente a la cámara. */
class GenerarQuizDeCamaraUseCaseTest {

    private fun caso(vararg senas: Sena) = GenerarQuizDeCamaraUseCase(
        FakeLeccionRepository(listOf(leccionDePrueba("leccion", senas.toList()))),
    )

    private val vocales = arrayOf(
        senaDePrueba("A", orden = 1),
        senaDePrueba("I", orden = 2),
        senaDePrueba("O", orden = 3),
        senaDePrueba("U", orden = 4),
    )

    private fun List<PreguntaDeCamara>.ids() = map { it.senaId }

    private fun List<String>.sinRepetirSeguidas() = zipWithNext().none { (a, b) -> a == b }

    @Test
    fun `por defecto son cinco preguntas`() = runTest {
        val quiz = caso(*vocales)("leccion", random = Random(1))

        assertEquals(GenerarQuizDeCamaraUseCase.PREGUNTAS_POR_QUIZ, quiz.size)
        assertEquals(5, quiz.size)
    }

    @Test
    fun `cada letra de la leccion sale al menos una vez`() = runTest {
        repeat(30) { semilla ->
            val quiz = caso(*vocales)("leccion", random = Random(semilla))

            assertEquals(setOf("A", "I", "O", "U"), quiz.ids().toSet())
        }
    }

    @Test
    fun `una letra nunca sale en dos preguntas seguidas`() = runTest {
        repeat(50) { semilla ->
            listOf(vocales.toList(), vocales.take(3), vocales.take(2)).forEach { senas ->
                val quiz = caso(*senas.toTypedArray())("leccion", random = Random(semilla))

                assertTrue("semilla $semilla: ${quiz.ids()}", quiz.ids().sinRepetirSeguidas())
            }
        }
    }

    @Test
    fun `las repeticiones se reparten sin cargar una sola letra`() = runTest {
        repeat(30) { semilla ->
            val quiz = caso(*vocales.take(2).toTypedArray())("leccion", random = Random(semilla))
            val veces = quiz.ids().groupingBy { it }.eachCount().values

            // Cinco preguntas con dos letras: tres y dos, nunca cuatro y una.
            assertEquals(setOf(2, 3), veces.toSet())
        }
    }

    @Test
    fun `muestra la letra y evalua por el identificador de la sena`() = runTest {
        val ene = senaDePrueba("N", orden = 1).copy(letra = "N mayúscula")

        val quiz = caso(ene, senaDePrueba("A", orden = 2))("leccion", random = Random(3))

        assertTrue(PreguntaDeCamara("N", "N mayúscula") in quiz)
    }

    @Test
    fun `las letras fuera del vocabulario no se preguntan`() = runTest {
        val quiz = caso(senaDePrueba("A", orden = 1), senaDePrueba("NN", orden = 2, disponible = false))(
            "leccion",
            random = Random(2),
        )

        assertTrue(quiz.none { it.senaId == "NN" })
    }

    @Test
    fun `una leccion de una sola letra la repite porque no hay otra`() = runTest {
        val quiz = caso(senaDePrueba("A"))("leccion", random = Random(1))

        assertEquals(List(5) { "A" }, quiz.ids())
    }

    @Test
    fun `se pueden pedir menos preguntas que letras`() = runTest {
        val quiz = caso(*vocales)("leccion", numeroDePreguntas = 2, random = Random(4))

        assertEquals(2, quiz.size)
        assertEquals(2, quiz.ids().distinct().size)
    }

    @Test
    fun `con la misma semilla sale el mismo quiz`() = runTest {
        assertEquals(
            caso(*vocales)("leccion", random = Random(42)),
            caso(*vocales)("leccion", random = Random(42)),
        )
    }

    @Test
    fun `sin letras practicables o sin leccion no hay quiz`() = runTest {
        assertTrue(caso(senaDePrueba("NN", disponible = false))("leccion").isEmpty())
        assertTrue(caso(*vocales)("no_existe").isEmpty())
        assertTrue(caso(*vocales)("leccion", numeroDePreguntas = 0).isEmpty())
    }
}
