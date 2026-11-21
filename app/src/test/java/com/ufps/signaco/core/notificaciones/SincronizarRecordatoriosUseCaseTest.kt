package com.ufps.signaco.core.notificaciones

import com.ufps.signaco.core.notificaciones.domain.SincronizarRecordatoriosUseCase
import com.ufps.signaco.fakes.FakePreferenciasRepository
import com.ufps.signaco.fakes.FakeProgramadorDeRecordatorios
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SincronizarRecordatoriosUseCaseTest {

    private val preferencias = FakePreferenciasRepository()
    private val programador = FakeProgramadorDeRecordatorios()
    private val useCase = SincronizarRecordatoriosUseCase(preferencias, programador)

    @Test
    fun `con recordatorios activos programa la alarma diaria`() = runTest {
        preferencias.cambiarRecordatoriosActivos(true)

        useCase()

        assertTrue(programador.programado)
    }

    @Test
    fun `con recordatorios inactivos cancela la alarma diaria`() = runTest {
        preferencias.cambiarRecordatoriosActivos(false)
        programador.programado = true

        useCase()

        assertFalse(programador.programado)
    }
}
