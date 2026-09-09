package com.ufps.signaco.feature.practica

import com.ufps.signaco.core.reconocimiento.data.FakeSignClassifier
import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.fakes.FakeRachaRepository
import com.ufps.signaco.fakes.RelojFijo
import com.ufps.signaco.feature.practica.domain.model.ResultadoIntento
import com.ufps.signaco.feature.practica.domain.model.TipoDeSimulacion
import com.ufps.signaco.feature.practica.domain.usecase.EvaluarIntentoUseCase
import com.ufps.signaco.feature.practica.domain.usecase.SimularIntentoUseCase
import com.ufps.signaco.feature.progreso.domain.usecase.ActualizarRachaUseCase
import com.ufps.signaco.feature.progreso.domain.usecase.RegistrarIntentoUseCase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprueba que el andamio de simulación recorre el mismo camino que recorrerá la cámara:
 * landmarks -> clasificador -> comparación -> progreso.
 */
class SimularIntentoUseCaseTest {

    private val dispatcher = StandardTestDispatcher()
    private val progreso = FakeProgresoRepository()

    private fun crearCaso() = SimularIntentoUseCase(
        EvaluarIntentoUseCase(
            clasificador = FakeSignClassifier(dispatcher),
            registrarIntento = RegistrarIntentoUseCase(
                progresoRepository = progreso,
                actualizarRacha = ActualizarRachaUseCase(
                    FakeRachaRepository(),
                    RelojFijo(dia = 1),
                ),
            ),
        ),
    )

    @Test
    fun `simular la sena correcta produce un acierto`() = runTest(dispatcher) {
        val resultado = crearCaso()("C", TipoDeSimulacion.SENA_CORRECTA)

        assertTrue(resultado is ResultadoIntento.Acierto)
        assertEquals("C", (resultado as ResultadoIntento.Acierto).letra)
    }

    @Test
    fun `simular otra letra produce una letra distinta`() = runTest(dispatcher) {
        val resultado = crearCaso()("C", TipoDeSimulacion.OTRA_LETRA)

        assertTrue(resultado is ResultadoIntento.OtraLetra)
    }

    @Test
    fun `simular una sena no reconocida no devuelve ninguna letra`() = runTest(dispatcher) {
        val resultado = crearCaso()("C", TipoDeSimulacion.NO_RECONOCIDA)

        assertEquals(ResultadoIntento.NoReconocida, resultado)
    }

    @Test
    fun `simular la mano fuera de cuadro no registra intento`() = runTest(dispatcher) {
        val resultado = crearCaso()("C", TipoDeSimulacion.MANO_FUERA)

        assertEquals(ResultadoIntento.SinMano, resultado)
        assertTrue(progreso.intentosRegistrados.isEmpty())
    }
}
