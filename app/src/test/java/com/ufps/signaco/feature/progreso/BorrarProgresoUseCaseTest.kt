package com.ufps.signaco.feature.progreso

import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import com.ufps.signaco.feature.progreso.domain.usecase.BorrarProgresoUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class BorrarProgresoUseCaseTest {

    @Test
    fun `borra los intentos y las lecciones completadas`() = runTest {
        val progreso = FakeProgresoRepository(
            progresoInicial = listOf(ProgresoSena("A", intentos = 3, aciertos = 2)),
            completadasIniciales = setOf("vocales"),
        )

        BorrarProgresoUseCase(progreso).invoke()

        assertTrue(progreso.progreso.value.isEmpty())
        assertTrue(progreso.completadas.value.isEmpty())
    }
}
