package com.ufps.signaco.feature.ajustes

import com.ufps.signaco.core.notificaciones.domain.SincronizarRecordatoriosUseCase
import com.ufps.signaco.core.preferencias.domain.ManoDominante
import com.ufps.signaco.core.preferencias.domain.VelocidadDeClips
import com.ufps.signaco.fakes.FakePreferenciasRepository
import com.ufps.signaco.fakes.FakeProgramadorDeRecordatorios
import com.ufps.signaco.fakes.ReglaDeDispatcherPrincipal
import com.ufps.signaco.feature.ajustes.presentation.AjustesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AjustesViewModelTest {

    @get:Rule
    val reglaDispatcher = ReglaDeDispatcherPrincipal()

    private val repositorio = FakePreferenciasRepository()
    private val programador = FakeProgramadorDeRecordatorios()
    private val sincronizarRecordatorios = SincronizarRecordatoriosUseCase(repositorio, programador)

    private fun crearViewModel() = AjustesViewModel(repositorio, sincronizarRecordatorios)

    @Test
    fun `activar recordatorios actualiza preferencias y programa recordatorio diario`() = runTest {
        val viewModel = crearViewModel()

        viewModel.cambiarRecordatorios(true)
        advanceUntilIdle()

        assertTrue(repositorio.preferencias.value.recordatoriosActivos)
        assertTrue(programador.programado)
    }

    @Test
    fun `desactivar recordatorios actualiza preferencias y cancela la alarma`() = runTest {
        repositorio.cambiarRecordatoriosActivos(true)
        programador.programado = true
        val viewModel = crearViewModel()

        viewModel.cambiarRecordatorios(false)
        advanceUntilIdle()

        assertFalse(repositorio.preferencias.value.recordatoriosActivos)
        assertFalse(programador.programado)
    }

    @Test
    fun `cambiar mano dominante actualiza preferencias`() = runTest {
        val viewModel = crearViewModel()

        viewModel.cambiarManoDominante(ManoDominante.IZQUIERDA)
        advanceUntilIdle()

        assertEquals(ManoDominante.IZQUIERDA, repositorio.preferencias.value.manoDominante)
    }

    @Test
    fun `cambiar velocidad de clips actualiza preferencias`() = runTest {
        val viewModel = crearViewModel()

        viewModel.cambiarVelocidadDeClips(VelocidadDeClips.LENTA)
        advanceUntilIdle()

        assertEquals(VelocidadDeClips.LENTA, repositorio.preferencias.value.velocidadDeClips)
    }

    @Test
    fun `cambiar vibracion actualiza preferencias`() = runTest {
        val viewModel = crearViewModel()

        viewModel.cambiarVibracion(false)
        advanceUntilIdle()

        assertFalse(repositorio.preferencias.value.retroalimentacionHaptica)
    }

    @Test
    fun `cambiar subtitulos actualiza preferencias`() = runTest {
        val viewModel = crearViewModel()

        viewModel.cambiarSubtitulos(true)
        advanceUntilIdle()

        assertTrue(repositorio.preferencias.value.subtitulosEnClips)
    }
}
