package com.ufps.signaco.feature.progreso

import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.fakes.FakeRachaRepository
import com.ufps.signaco.fakes.RelojFijo
import com.ufps.signaco.feature.progreso.domain.usecase.ActualizarRachaUseCase
import com.ufps.signaco.feature.progreso.domain.usecase.RegistrarIntentoUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrarIntentoUseCaseTest {

    @Test
    fun `registrar un acierto guarda el intento y marca el dia como activo`() = runTest {
        val progreso = FakeProgresoRepository()
        val racha = FakeRachaRepository()
        val caso = RegistrarIntentoUseCase(
            progresoRepository = progreso,
            actualizarRacha = ActualizarRachaUseCase(racha, RelojFijo(dia = 10)),
        )

        caso("A", acerto = true)

        assertEquals(listOf("A" to true), progreso.intentosRegistrados)
        assertEquals(1, progreso.progreso.value.single().intentos)
        assertEquals(1, progreso.progreso.value.single().aciertos)
        assertEquals(1, racha.racha.value.diasConsecutivos)
    }

    @Test
    fun `un fallo suma intento pero no acierto`() = runTest {
        val progreso = FakeProgresoRepository()
        val caso = RegistrarIntentoUseCase(
            progresoRepository = progreso,
            actualizarRacha = ActualizarRachaUseCase(FakeRachaRepository(), RelojFijo(dia = 10)),
        )

        caso("B", acerto = false)

        assertEquals(1, progreso.progreso.value.single().intentos)
        assertEquals(0, progreso.progreso.value.single().aciertos)
    }
}
