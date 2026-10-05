package com.ufps.signaco.feature.lecciones

import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.fakes.FakeLeccionRepository
import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.fakes.leccionDePrueba
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.lecciones.domain.model.CategoriaConLecciones
import com.ufps.signaco.feature.lecciones.domain.usecase.CombinarLeccionConProgresoUseCase
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerCatalogoDeLeccionesUseCase
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObtenerCatalogoDeLeccionesUseCaseTest {

    private val vocales = leccionDePrueba(
        id = "vocales",
        senas = listOf(senaDePrueba("A", orden = 1), senaDePrueba("E", orden = 2)),
        categoria = Categoria.VOCALES,
        orden = 1,
    )

    private val consonantes = leccionDePrueba(
        id = "consonantes",
        senas = listOf(senaDePrueba("B", orden = 3, categoria = Categoria.CONSONANTES)),
        categoria = Categoria.CONSONANTES,
        orden = 2,
    )

    private fun crearCaso(progreso: FakeProgresoRepository) = ObtenerCatalogoDeLeccionesUseCase(
        leccionRepository = FakeLeccionRepository(listOf(consonantes, vocales)),
        progresoRepository = progreso,
        combinarProgreso = CombinarLeccionConProgresoUseCase(),
    )

    @Test
    fun `agrupa las lecciones por categoria y respeta el orden del catalogo`() = runTest {
        val catalogo = crearCaso(FakeProgresoRepository()).invoke().first()

        assertEquals(
            listOf(Categoria.VOCALES, Categoria.CONSONANTES),
            catalogo.map { it.categoria },
        )
    }

    @Test
    fun `una sena sin practicar cuenta como progreso vacio, no como ausente`() = runTest {
        val catalogo = crearCaso(FakeProgresoRepository()).invoke().first()

        val leccionDeVocales = catalogo.first().lecciones.single()
        assertEquals(2, leccionDeVocales.totalDeSenas)
        assertEquals(0, leccionDeVocales.senasDominadas)
        assertEquals(0f, leccionDeVocales.porcentaje, 0.0001f)
    }

    @Test
    fun `el avance de la leccion cuenta solo las senas dominadas`() = runTest {
        val progreso = FakeProgresoRepository(
            progresoInicial = listOf(
                ProgresoSena("A", intentos = 8, aciertos = 7),
                ProgresoSena("E", intentos = 8, aciertos = 2),
            ),
        )

        val catalogo = crearCaso(progreso).invoke().first()

        val leccionDeVocales = catalogo.first().lecciones.single()
        assertEquals(1, leccionDeVocales.senasDominadas)
        assertEquals(0.5f, leccionDeVocales.porcentaje, 0.0001f)
    }

    @Test
    fun `una leccion aparece como completada solo si esta en la lista de completadas`() = runTest {
        val progreso = FakeProgresoRepository(completadasIniciales = setOf("vocales"))

        val catalogo = crearCaso(progreso).invoke().first()

        assertTrue(catalogo.first().lecciones.single().completada)
        assertFalse(catalogo.last().lecciones.single().completada)
    }

    private fun enCurso(catalogo: List<CategoriaConLecciones>): List<String> =
        catalogo.flatMap { it.lecciones }.filter { it.enCurso }.map { it.leccion.id }

    @Test
    fun `sin progreso la leccion en curso es la primera`() = runTest {
        val catalogo = crearCaso(FakeProgresoRepository()).invoke().first()

        assertEquals(listOf("vocales"), enCurso(catalogo))
    }

    @Test
    fun `la leccion en curso es la primera empezada y sin completar`() = runTest {
        val progreso = FakeProgresoRepository(
            progresoInicial = listOf(ProgresoSena("B", intentos = 2, aciertos = 1)),
        )

        val catalogo = crearCaso(progreso).invoke().first()

        assertEquals(listOf("consonantes"), enCurso(catalogo))
    }

    @Test
    fun `si todas estan completas no hay leccion en curso`() = runTest {
        val progreso = FakeProgresoRepository(completadasIniciales = setOf("vocales", "consonantes"))

        val catalogo = crearCaso(progreso).invoke().first()

        assertTrue(enCurso(catalogo).isEmpty())
    }
}
