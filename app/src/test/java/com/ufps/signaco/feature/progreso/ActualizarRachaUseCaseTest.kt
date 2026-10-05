package com.ufps.signaco.feature.progreso

import com.ufps.signaco.fakes.FakeRachaRepository
import com.ufps.signaco.fakes.RelojFijo
import com.ufps.signaco.feature.progreso.domain.model.Racha
import com.ufps.signaco.feature.progreso.domain.usecase.ActualizarRachaUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ActualizarRachaUseCaseTest {

    @Test
    fun `la primera actividad arranca la racha en uno`() = runTest {
        val repositorio = FakeRachaRepository(Racha.INICIAL)
        val caso = ActualizarRachaUseCase(repositorio, RelojFijo(dia = 100))

        val resultado = caso()

        assertEquals(1, resultado.diasConsecutivos)
        assertEquals(1, resultado.mejorRacha)
        assertEquals(100L, resultado.ultimoDiaActivo)
    }

    @Test
    fun `practicar dos veces el mismo dia no suma a la racha`() = runTest {
        val repositorio = FakeRachaRepository(
            Racha(diasConsecutivos = 3, mejorRacha = 5, ultimoDiaActivo = 100),
        )
        val caso = ActualizarRachaUseCase(repositorio, RelojFijo(dia = 100))

        val resultado = caso()

        assertEquals(3, resultado.diasConsecutivos)
        assertEquals(5, resultado.mejorRacha)
    }

    @Test
    fun `practicar al dia siguiente suma uno`() = runTest {
        val repositorio = FakeRachaRepository(
            Racha(diasConsecutivos = 3, mejorRacha = 3, ultimoDiaActivo = 100),
        )
        val caso = ActualizarRachaUseCase(repositorio, RelojFijo(dia = 101))

        val resultado = caso()

        assertEquals(4, resultado.diasConsecutivos)
        assertEquals(4, resultado.mejorRacha)
    }

    @Test
    fun `saltarse un dia reinicia la racha en uno`() = runTest {
        val repositorio = FakeRachaRepository(
            Racha(diasConsecutivos = 7, mejorRacha = 7, ultimoDiaActivo = 100),
        )
        val caso = ActualizarRachaUseCase(repositorio, RelojFijo(dia = 103))

        val resultado = caso()

        assertEquals(1, resultado.diasConsecutivos)
    }

    @Test
    fun `la mejor racha nunca baja`() = runTest {
        val repositorio = FakeRachaRepository(
            Racha(diasConsecutivos = 7, mejorRacha = 9, ultimoDiaActivo = 100),
        )
        val caso = ActualizarRachaUseCase(repositorio, RelojFijo(dia = 105))

        val resultado = caso()

        assertEquals(1, resultado.diasConsecutivos)
        assertEquals(9, resultado.mejorRacha)
    }

    @Test
    fun `la racha actualizada queda guardada`() = runTest {
        val repositorio = FakeRachaRepository(Racha.INICIAL)
        val caso = ActualizarRachaUseCase(repositorio, RelojFijo(dia = 42))

        caso()

        assertEquals(42L, repositorio.racha.value.ultimoDiaActivo)
        assertEquals(1, repositorio.racha.value.diasConsecutivos)
    }

    @Test
    fun `la actividad deja marcado el dia para la racha semanal`() = runTest {
        val repositorio = FakeRachaRepository(Racha.INICIAL)

        ActualizarRachaUseCase(repositorio, RelojFijo(dia = 100)).invoke()

        assertEquals(setOf(100L), repositorio.diasActivos.value)
    }
}
