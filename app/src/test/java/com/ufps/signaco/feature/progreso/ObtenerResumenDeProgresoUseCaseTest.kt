package com.ufps.signaco.feature.progreso

import app.cash.turbine.test
import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.fakes.FakeRachaRepository
import com.ufps.signaco.fakes.FakeSenaRepository
import com.ufps.signaco.fakes.RelojFijo
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import com.ufps.signaco.feature.progreso.domain.model.Racha
import com.ufps.signaco.feature.progreso.domain.model.ResumenProgreso
import com.ufps.signaco.feature.progreso.domain.usecase.ObtenerResumenDeProgresoUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObtenerResumenDeProgresoUseCaseTest {

    private val senas = listOf(
        senaDePrueba("A", orden = 1),
        senaDePrueba("B", orden = 2),
        senaDePrueba("NN", orden = 3, disponible = false),
    )

    @Test
    fun `las senas fuera del MVP no entran en el denominador`() = runTest {
        val caso = ObtenerResumenDeProgresoUseCase(
            senaRepository = FakeSenaRepository(senas),
            progresoRepository = FakeProgresoRepository(),
            rachaRepository = FakeRachaRepository(),
            reloj = RelojFijo(dia = 100),
        )

        val resumen = caso().first()

        assertEquals(2, resumen.senasDisponibles)
    }

    @Test
    fun `cuenta las senas dominadas y las lecciones completadas`() = runTest {
        val caso = ObtenerResumenDeProgresoUseCase(
            senaRepository = FakeSenaRepository(senas),
            progresoRepository = FakeProgresoRepository(
                progresoInicial = listOf(
                    ProgresoSena("A", intentos = 8, aciertos = 7),
                    ProgresoSena("B", intentos = 4, aciertos = 1),
                ),
                completadasIniciales = setOf("leccion_vocales"),
            ),
            rachaRepository = FakeRachaRepository(
                Racha(diasConsecutivos = 4, mejorRacha = 9, ultimoDiaActivo = 100),
            ),
            reloj = RelojFijo(dia = 100),
        )

        val resumen = caso().first()

        assertEquals(1, resumen.senasDominadas)
        assertEquals(1, resumen.leccionesCompletadas)
        assertEquals(4, resumen.rachaActual)
        assertEquals(9, resumen.mejorRacha)
        assertEquals(0.5f, resumen.porcentajeDominado, 0.0001f)
    }

    @Test
    fun `el detalle solo trae las senas que se han practicado`() = runTest {
        val caso = ObtenerResumenDeProgresoUseCase(
            senaRepository = FakeSenaRepository(senas),
            progresoRepository = FakeProgresoRepository(
                progresoInicial = listOf(ProgresoSena("A", intentos = 2, aciertos = 1)),
            ),
            rachaRepository = FakeRachaRepository(),
            reloj = RelojFijo(dia = 100),
        )

        val resumen = caso().first()

        assertEquals(listOf("A"), resumen.avancePorSena.map { it.sena.id })
    }

    @Test
    fun `el resumen se vuelve a emitir cuando cambia el progreso`() = runTest {
        val progreso = FakeProgresoRepository()
        val caso = ObtenerResumenDeProgresoUseCase(
            senaRepository = FakeSenaRepository(senas),
            progresoRepository = progreso,
            rachaRepository = FakeRachaRepository(),
            reloj = RelojFijo(dia = 100),
        )

        caso().test {
            assertEquals(0, awaitItem().avancePorSena.size)

            progreso.registrarIntento("A", acerto = true)

            assertEquals(1, awaitItem().avancePorSena.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `el lunes de la semana se calcula desde el jueves 1 de enero de 1970`() {
        // Dia 0 fue jueves: su lunes es el dia -3. El dia 4 fue lunes.
        assertEquals(-3L, ResumenProgreso.lunesDeLaSemana(0))
        assertEquals(4L, ResumenProgreso.lunesDeLaSemana(4))
        assertEquals(4L, ResumenProgreso.lunesDeLaSemana(10))
    }

    @Test
    fun `marca los dias activos de la semana actual de lunes a domingo`() = runTest {
        // Dia 104 es miercoles (lunes = 102). El 90 es de otra semana y no cuenta.
        val racha = FakeRachaRepository().apply { diasActivos.value = setOf(90L, 102L, 103L, 107L) }
        val caso = ObtenerResumenDeProgresoUseCase(
            senaRepository = FakeSenaRepository(senas),
            progresoRepository = FakeProgresoRepository(),
            rachaRepository = racha,
            reloj = RelojFijo(dia = 104),
        )

        val resumen = caso().first()

        assertEquals(
            listOf(true, true, false, false, false, true, false),
            resumen.actividadDeLaSemana,
        )
    }
}
