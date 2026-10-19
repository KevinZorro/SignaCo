package com.ufps.signaco.feature.quiz

import com.ufps.signaco.fakes.FakeLeccionRepository
import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.fakes.leccionDePrueba
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.lecciones.domain.usecase.CombinarLeccionConProgresoUseCase
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerDetalleDeLeccionUseCase
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import com.ufps.signaco.feature.quiz.domain.usecase.ObtenerDesgloseDeQuizUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObtenerDesgloseDeQuizUseCaseTest {

    private val leccion = leccionDePrueba(
        id = "vocales",
        senas = listOf(senaDePrueba("A", orden = 1), senaDePrueba("I", orden = 2), senaDePrueba("O", orden = 3)),
    )

    private fun crearCaso(progreso: FakeProgresoRepository = FakeProgresoRepository()) =
        ObtenerDesgloseDeQuizUseCase(
            ObtenerDetalleDeLeccionUseCase(
                leccionRepository = FakeLeccionRepository(listOf(leccion)),
                progresoRepository = progreso,
                combinarProgreso = CombinarLeccionConProgresoUseCase(),
            ),
        )

    @Test
    fun `respeta el orden del quiz y marca las falladas`() = runTest {
        val desglose = crearCaso()("vocales", listOf("O", "A"), listOf(true, false)).first()!!

        assertEquals(listOf("O", "A"), desglose.senas.map { it.sena.id })
        assertEquals(listOf(true, false), desglose.senas.map { it.acerto })
        assertEquals(listOf("A"), desglose.porRepasar.map { it.id })
    }

    @Test
    fun `agrupa una letra que salio varias veces y cuenta sus aciertos`() = runTest {
        val desglose = crearCaso()(
            "vocales",
            listOf("A", "I", "A", "O", "A"),
            listOf(true, true, false, true, true),
        ).first()!!

        assertEquals(listOf("A", "I", "O"), desglose.senas.map { it.sena.id })
        val a = desglose.senas.first()
        assertEquals(3, a.preguntas)
        assertEquals(2, a.aciertos)
        assertEquals(listOf("A"), desglose.porRepasar.map { it.id })
    }

    @Test
    fun `informa si la sena ya estaba dominada`() = runTest {
        val progreso = FakeProgresoRepository(
            progresoInicial = listOf(ProgresoSena("I", intentos = 6, aciertos = 6)),
        )

        val desglose = crearCaso(progreso)("vocales", listOf("A", "I"), listOf(true, true)).first()!!

        assertEquals(listOf(false, true), desglose.senas.map { it.dominada })
    }

    @Test
    fun `una leccion que no existe no tiene desglose`() = runTest {
        assertNull(crearCaso()("no_existe", listOf("A"), listOf(true)).first())
    }
}
