package com.ufps.signaco.feature.practica

import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import com.ufps.signaco.fakes.FakeClasificadorProgramable
import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.fakes.FakeRachaRepository
import com.ufps.signaco.fakes.RelojFijo
import com.ufps.signaco.feature.practica.domain.model.ResultadoIntento
import com.ufps.signaco.feature.practica.domain.usecase.EvaluarIntentoUseCase
import com.ufps.signaco.feature.progreso.domain.usecase.ActualizarRachaUseCase
import com.ufps.signaco.feature.progreso.domain.usecase.RegistrarIntentoUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Estas pruebas también sirven de demostración del RNF-21: el caso de uso se prueba entero
 * con un clasificador de mentira, sin modelo ni cámara.
 */
class EvaluarIntentoUseCaseTest {

    private val clasificador = FakeClasificadorProgramable()
    private val progreso = FakeProgresoRepository()

    private val caso = EvaluarIntentoUseCase(
        clasificador = clasificador,
        registrarIntento = RegistrarIntentoUseCase(
            progresoRepository = progreso,
            actualizarRacha = ActualizarRachaUseCase(FakeRachaRepository(), RelojFijo(dia = 1)),
        ),
    )

    @Test
    fun `si el modelo reconoce la letra que se practica es un acierto`() = runTest {
        clasificador.siguienteResultado =
            ResultadoClasificacion.LetraReconocida("A", confianza = 0.93f)

        val resultado = caso("A", FloatArray(63))

        assertTrue(resultado is ResultadoIntento.Acierto)
        assertEquals(0.93f, (resultado as ResultadoIntento.Acierto).confianza, 0.0001f)
        assertEquals(listOf("A" to true), progreso.intentosRegistrados)
    }

    @Test
    fun `si el modelo reconoce otra letra no es acierto pero si cuenta como intento`() = runTest {
        clasificador.siguienteResultado =
            ResultadoClasificacion.LetraReconocida("S", confianza = 0.88f)

        val resultado = caso("A", FloatArray(63))

        assertEquals(ResultadoIntento.OtraLetra("S", 0.88f), resultado)
        assertEquals(listOf("A" to false), progreso.intentosRegistrados)
    }

    @Test
    fun `una sena no reconocida cuenta como intento fallido`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.SenaNoReconocida

        val resultado = caso("A", FloatArray(63))

        assertEquals(ResultadoIntento.NoReconocida, resultado)
        assertEquals(listOf("A" to false), progreso.intentosRegistrados)
    }

    @Test
    fun `sin mano en cuadro no hubo intento y no se registra nada`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.ManoNoDetectada

        val resultado = caso("A", FloatArray(0))

        assertEquals(ResultadoIntento.SinMano, resultado)
        assertTrue(progreso.intentosRegistrados.isEmpty())
    }
}
