package com.ufps.signaco.core.reconocimiento

import com.ufps.signaco.core.reconocimiento.data.FakeSignClassifier
import com.ufps.signaco.core.reconocimiento.data.LandmarksSimulados
import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeSignClassifierTest {

    // El dispatcher de prueba comparte reloj con runTest, así el retardo simulado del
    // clasificador se salta en vez de esperarse de verdad.
    private val dispatcher = StandardTestDispatcher()
    private val clasificador = FakeSignClassifier(dispatcher)

    @Test
    fun `un arreglo vacio significa que no hay mano`() = runTest(dispatcher) {
        val resultado = clasificador.clasificar(LandmarksSimulados.manoFueraDeCuadro())

        assertEquals(ResultadoClasificacion.ManoNoDetectada, resultado)
    }

    @Test
    fun `un arreglo con tamano equivocado tambien se trata como sin mano`() = runTest(dispatcher) {
        val resultado = clasificador.clasificar(FloatArray(10))

        assertEquals(ResultadoClasificacion.ManoNoDetectada, resultado)
    }

    @Test
    fun `un frame marcado como no reconocido devuelve sena no reconocida`() = runTest(dispatcher) {
        val resultado = clasificador.clasificar(LandmarksSimulados.senaNoReconocida())

        assertEquals(ResultadoClasificacion.SenaNoReconocida(), resultado)
    }

    @Test
    fun `un frame que codifica una letra devuelve esa letra`() = runTest(dispatcher) {
        val resultado = clasificador.clasificar(LandmarksSimulados.senaDe("K"))

        assertTrue(resultado is ResultadoClasificacion.LetraReconocida)
        assertEquals("K", (resultado as ResultadoClasificacion.LetraReconocida).letra)
    }

    @Test
    fun `la confianza simulada es estable entre llamadas`() = runTest(dispatcher) {
        val primera = clasificador.clasificar(LandmarksSimulados.senaDe("M"))
        val segunda = clasificador.clasificar(LandmarksSimulados.senaDe("M"))

        assertEquals(
            (primera as ResultadoClasificacion.LetraReconocida).confianza,
            (segunda as ResultadoClasificacion.LetraReconocida).confianza,
            0.0001f,
        )
        assertTrue(primera.confianza in 0f..1f)
    }
}
