package com.ufps.signaco.feature.progreso

import com.ufps.signaco.core.senas.domain.SenaRepository
import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.fakes.FakeRachaRepository
import com.ufps.signaco.fakes.FakeSenaRepository
import com.ufps.signaco.fakes.RelojFijo
import com.ufps.signaco.fakes.ReglaDeDispatcherPrincipal
import com.ufps.signaco.fakes.SenaRepositoryQueFalla
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import com.ufps.signaco.feature.progreso.domain.model.Racha
import com.ufps.signaco.feature.progreso.domain.usecase.ObtenerResumenDeProgresoUseCase
import com.ufps.signaco.feature.progreso.presentation.ProgresoViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProgresoViewModelTest {

    @get:Rule
    val reglaDeDispatcher = ReglaDeDispatcherPrincipal()

    private val senas = listOf(senaDePrueba("A", orden = 1), senaDePrueba("B", orden = 2))

    private fun crearViewModel(
        senaRepository: SenaRepository = FakeSenaRepository(senas),
        progreso: ProgresoRepository = FakeProgresoRepository(),
    ) = ProgresoViewModel(
        ObtenerResumenDeProgresoUseCase(
            senaRepository = senaRepository,
            progresoRepository = progreso,
            rachaRepository = FakeRachaRepository(
                Racha(diasConsecutivos = 2, mejorRacha = 6, ultimoDiaActivo = 100),
            ),
            reloj = RelojFijo(dia = 100),
        ),
    )

    @Test
    fun `publica el resumen con las cuentas del dominio`() = runTest {
        val viewModel = crearViewModel(
            progreso = FakeProgresoRepository(
                progresoInicial = listOf(ProgresoSena("A", intentos = 8, aciertos = 7)),
                completadasIniciales = setOf("vocales"),
            ),
        )

        val estado = viewModel.uiState.first { !it.cargando }

        assertFalse(estado.hayError)
        assertEquals(1, estado.resumen.senasDominadas)
        assertEquals(2, estado.resumen.senasDisponibles)
        assertEquals(1, estado.resumen.leccionesCompletadas)
        assertEquals(2, estado.resumen.rachaActual)
        assertEquals(6, estado.resumen.mejorRacha)
    }

    @Test
    fun `si falla la carga el estado queda en error y no en carga infinita`() = runTest {
        val viewModel = crearViewModel(senaRepository = SenaRepositoryQueFalla())

        val estado = viewModel.uiState.first { !it.cargando }

        assertTrue(estado.hayError)
    }
}
