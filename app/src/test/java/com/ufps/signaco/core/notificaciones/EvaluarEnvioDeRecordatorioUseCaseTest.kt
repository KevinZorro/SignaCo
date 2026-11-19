package com.ufps.signaco.core.notificaciones

import com.ufps.signaco.core.notificaciones.domain.DecisionDeRecordatorio
import com.ufps.signaco.core.notificaciones.domain.EvaluarEnvioDeRecordatorioUseCase
import com.ufps.signaco.core.notificaciones.domain.MotivoNoEnvio
import com.ufps.signaco.fakes.FakePreferenciasRepository
import com.ufps.signaco.fakes.FakeRachaRepository
import com.ufps.signaco.fakes.RelojFijo
import com.ufps.signaco.feature.progreso.domain.model.Racha
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluarEnvioDeRecordatorioUseCaseTest {

    private val preferencias = FakePreferenciasRepository()
    private val racha = FakeRachaRepository()
    private val reloj = RelojFijo(dia = 100L)

    private val useCase = EvaluarEnvioDeRecordatorioUseCase(
        preferenciasRepository = preferencias,
        rachaRepository = racha,
        reloj = reloj,
    )

    @Test
    fun `no envia si los recordatorios estan desactivados en preferencias`() = runTest {
        preferencias.cambiarRecordatoriosActivos(false)

        val resultado = useCase()

        assertEquals(
            DecisionDeRecordatorio.NoEnviar(MotivoNoEnvio.RECORDATORIOS_DESACTIVADOS),
            resultado,
        )
    }

    @Test
    fun `no envia si el usuario ya registro actividad hoy`() = runTest {
        preferencias.cambiarRecordatoriosActivos(true)
        racha.guardarRacha(Racha(diasConsecutivos = 2, mejorRacha = 2, ultimoDiaActivo = 100L))

        val resultado = useCase()

        assertEquals(
            DecisionDeRecordatorio.NoEnviar(MotivoNoEnvio.YA_PRACTICO_HOY),
            resultado,
        )
    }

    @Test
    fun `envia recordatorio si estan activos y no ha practicado hoy`() = runTest {
        preferencias.cambiarRecordatoriosActivos(true)
        // La última actividad fue ayer (día 99)
        racha.guardarRacha(Racha(diasConsecutivos = 1, mejorRacha = 1, ultimoDiaActivo = 99L))

        val resultado = useCase()

        assertTrue(resultado is DecisionDeRecordatorio.Enviar)
        val decision = resultado as DecisionDeRecordatorio.Enviar
        assertEquals((100L % 5).toInt(), decision.indiceMensaje)
    }

    @Test
    fun `el indice del mensaje rota segun el reloj del dia deterministamente`() = runTest {
        preferencias.cambiarRecordatoriosActivos(true)
        racha.guardarRacha(Racha(diasConsecutivos = 1, mejorRacha = 1, ultimoDiaActivo = -1L))

        for (dia in 0L..9L) {
            reloj.dia = dia
            val resultado = useCase()
            assertTrue(resultado is DecisionDeRecordatorio.Enviar)
            assertEquals((dia % 5).toInt(), (resultado as DecisionDeRecordatorio.Enviar).indiceMensaje)
        }
    }
}
