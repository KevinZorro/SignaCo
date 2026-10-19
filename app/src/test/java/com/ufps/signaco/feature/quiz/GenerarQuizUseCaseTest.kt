package com.ufps.signaco.feature.quiz

import com.ufps.signaco.fakes.FakeLeccionRepository
import com.ufps.signaco.fakes.FakeSenaRepository
import com.ufps.signaco.fakes.imagenDePrueba
import com.ufps.signaco.fakes.leccionDePrueba
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.lecciones.domain.model.Leccion
import com.ufps.signaco.feature.quiz.domain.usecase.GenerarQuizUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Quiz de reconocimiento: la imagen de una seña y cuatro letras para elegir. */
class GenerarQuizUseCaseTest {

    private fun conImagen(id: String, orden: Int) =
        senaDePrueba(id, orden = orden, referencia = imagenDePrueba(id))

    private val vocabulario = listOf(
        conImagen("A", 1),
        conImagen("B", 2),
        conImagen("C", 3),
        conImagen("D", 4),
        conImagen("E", 5),
        conImagen("F", 6),
        conImagen("G", 7),
        conImagen("H", 8),
        senaDePrueba("NN", orden = 9, disponible = false, referencia = imagenDePrueba("NN")),
    )

    /** A, B, C, D y E con imagen; NN fuera del MVP. */
    private val leccion = leccionDePrueba(
        id = "leccion_1",
        senas = vocabulario.take(5) + vocabulario.last(),
    )

    private fun crearCaso(leccionDelCaso: Leccion = leccion) =
        GenerarQuizUseCase(
            leccionRepository = FakeLeccionRepository(listOf(leccionDelCaso)),
            senaRepository = FakeSenaRepository(vocabulario),
        )

    @Test
    fun `genera tantas preguntas como se piden`() = runTest {
        val preguntas = crearCaso()("leccion_1", numeroDePreguntas = 3, random = Random(1))

        assertEquals(3, preguntas.size)
    }

    @Test
    fun `por defecto pregunta hasta cuatro letras de la leccion`() = runTest {
        val preguntas = crearCaso()("leccion_1", random = Random(1))

        assertEquals(GenerarQuizUseCase.PREGUNTAS_POR_QUIZ, preguntas.size)
        assertEquals(4, preguntas.size)
    }

    @Test
    fun `nunca pide mas preguntas que senas practicables tenga la leccion`() = runTest {
        val preguntas = crearCaso()("leccion_1", numeroDePreguntas = 20, random = Random(1))

        // La lección tiene 6 señas, pero una está fuera del MVP.
        assertEquals(5, preguntas.size)
    }

    @Test
    fun `no repite una letra en el mismo quiz`() = runTest {
        val preguntas = crearCaso()("leccion_1", numeroDePreguntas = 20, random = Random(5))

        assertEquals(preguntas.size, preguntas.map { it.senaId }.distinct().size)
    }

    @Test
    fun `cada pregunta muestra la imagen de su propia sena`() = runTest {
        val preguntas = crearCaso()("leccion_1", numeroDePreguntas = 5, random = Random(2))

        preguntas.forEach { assertEquals(imagenDePrueba(it.senaId), it.referencia) }
    }

    @Test
    fun `las letras sin imagen no se preguntan`() = runTest {
        val leccionConHuecos = leccionDePrueba(
            id = "leccion_1",
            senas = listOf(conImagen("A", 1), senaDePrueba("B", orden = 2), conImagen("C", 3)),
        )

        val preguntas = crearCaso(leccionConHuecos)("leccion_1", numeroDePreguntas = 5, random = Random(4))

        assertEquals(setOf("A", "C"), preguntas.map { it.senaId }.toSet())
    }

    @Test
    fun `una leccion sin imagenes no genera preguntas`() = runTest {
        val sinImagenes = leccionDePrueba(
            id = "leccion_1",
            senas = listOf(senaDePrueba("A", orden = 1), senaDePrueba("B", orden = 2)),
        )

        val preguntas = crearCaso(sinImagenes)("leccion_1", random = Random(1))

        assertTrue(preguntas.isEmpty())
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
    fun `los distractores salen primero de la misma leccion`() = runTest {
        // La lección tiene cinco letras: siempre hay al menos tres compañeras para confundir.
        val deLaLeccion = setOf("A", "B", "C", "D", "E")
        repeat(20) { semilla ->
            crearCaso()("leccion_1", numeroDePreguntas = 5, random = Random(semilla)).forEach { pregunta ->
                assertTrue(pregunta.opciones.all { it in deLaLeccion })
            }
        }
    }

    @Test
    fun `si la leccion es corta los distractores se completan con el vocabulario`() = runTest {
        val corta = leccionDePrueba(id = "leccion_1", senas = listOf(conImagen("A", 1), conImagen("B", 2)))

        val pregunta = crearCaso(corta)("leccion_1", numeroDePreguntas = 1, random = Random(9)).single()

        assertEquals(GenerarQuizUseCase.OPCIONES_POR_PREGUNTA, pregunta.opciones.distinct().size)
        val companera = if (pregunta.letraCorrecta == "A") "B" else "A"
        assertTrue("la otra letra de la lección siempre es opción", companera in pregunta.opciones)
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
