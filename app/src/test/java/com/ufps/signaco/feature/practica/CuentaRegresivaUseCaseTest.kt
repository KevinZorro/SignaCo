package com.ufps.signaco.feature.practica

import app.cash.turbine.test
import com.ufps.signaco.feature.practica.domain.usecase.CuentaRegresivaUseCase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** La cuenta regresiva de 3 segundos antes de cada intento. */
class CuentaRegresivaUseCaseTest {

    private val cuenta = CuentaRegresivaUseCase()

    @Test
    fun `cuenta 3 2 1 y termina en el inicio del intento`() = runTest {
        assertEquals(listOf(3, 2, 1, CuentaRegresivaUseCase.INICIO), cuenta().toList())
    }

    @Test
    fun `cada numero dura un segundo y el intento empieza a los tres segundos`() = runTest {
        cuenta().test {
            assertEquals(3, awaitItem())
            assertEquals(0L, currentTime)
            assertEquals(2, awaitItem())
            assertEquals(1_000L, currentTime)
            assertEquals(1, awaitItem())
            assertEquals(2_000L, currentTime)
            assertEquals(CuentaRegresivaUseCase.INICIO, awaitItem())
            assertEquals(3_000L, currentTime)
            awaitComplete()
        }
    }

    @Test
    fun `se puede pedir otra duracion`() = runTest {
        assertEquals(listOf(1, CuentaRegresivaUseCase.INICIO), cuenta(segundos = 1).toList())
    }

    @Test
    fun `sin segundos el intento empieza de inmediato`() = runTest {
        assertEquals(listOf(CuentaRegresivaUseCase.INICIO), cuenta(segundos = 0).toList())
    }
}
