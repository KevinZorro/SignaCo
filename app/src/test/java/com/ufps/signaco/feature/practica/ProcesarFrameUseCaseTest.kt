package com.ufps.signaco.feature.practica

import com.ufps.signaco.core.reconocimiento.domain.NormalizadorDeLandmarks
import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import com.ufps.signaco.fakes.FakeClasificadorProgramable
import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.fakes.FakeRachaRepository
import com.ufps.signaco.fakes.RelojFijo
import com.ufps.signaco.feature.practica.domain.EstabilizadorDePredicciones
import com.ufps.signaco.feature.practica.domain.model.EstadoDeReconocimiento
import com.ufps.signaco.feature.practica.domain.model.FrameDeLandmarks
import com.ufps.signaco.feature.practica.domain.model.ResultadoDeFrame
import com.ufps.signaco.feature.practica.domain.model.ResultadoIntento
import com.ufps.signaco.feature.practica.domain.usecase.EvaluarIntentoUseCase
import com.ufps.signaco.feature.practica.domain.usecase.ProcesarFrameUseCase
import com.ufps.signaco.feature.progreso.domain.usecase.ActualizarRachaUseCase
import com.ufps.signaco.feature.progreso.domain.usecase.RegistrarIntentoUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La pieza que convierte 30 frames por segundo en intentos. Lo que más importa aquí es que un
 * intento se registre UNA vez, no k veces por segundo mientras el usuario sostiene la pose.
 */
class ProcesarFrameUseCaseTest {

    private val clasificador = FakeClasificadorProgramable()
    private val progreso = FakeProgresoRepository()

    private val caso = ProcesarFrameUseCase(
        clasificador = clasificador,
        evaluarIntento = EvaluarIntentoUseCase(
            clasificador = clasificador,
            registrarIntento = RegistrarIntentoUseCase(
                progresoRepository = progreso,
                actualizarRacha = ActualizarRachaUseCase(FakeRachaRepository(), RelojFijo(dia = 1)),
            ),
        ),
    )

    private val estabilizador = EstabilizadorDePredicciones(k = 3)
    private val frameConMano = FrameDeLandmarks(FloatArray(NormalizadorDeLandmarks.VALORES))

    @Test
    fun `sin mano en cuadro avisa y reinicia la racha`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.LetraReconocida("A", 0.9f)
        caso("A", frameConMano, estabilizador)
        caso("A", frameConMano, estabilizador)

        val resultado = caso("A", FrameDeLandmarks.SIN_MANO, estabilizador)

        assertEquals(
            ResultadoDeFrame.Buscando(EstadoDeReconocimiento.BUSCANDO_MANOS, 0, 3),
            resultado,
        )
        assertEquals(0, estabilizador.avance)
    }

    @Test
    fun `mientras no hay k frames iguales solo informa el avance`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.LetraReconocida("A", 0.9f)

        val primero = caso("A", frameConMano, estabilizador)

        assertEquals(
            ResultadoDeFrame.Buscando(EstadoDeReconocimiento.RECONOCIENDO, 1, 3, coincidencia = 0.9f),
            primero,
        )
        assertTrue(progreso.intentosRegistrados.isEmpty())
    }

    @Test
    fun `al estabilizarse una letra se confirma el intento y se registra una sola vez`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.LetraReconocida("A", 0.93f)

        caso("A", frameConMano, estabilizador)
        caso("A", frameConMano, estabilizador)
        val confirmado = caso("A", frameConMano, estabilizador)
        // El usuario sigue sosteniendo la seña.
        caso("A", frameConMano, estabilizador)
        caso("A", frameConMano, estabilizador)

        assertTrue(confirmado is ResultadoDeFrame.Confirmado)
        assertTrue((confirmado as ResultadoDeFrame.Confirmado).resultado is ResultadoIntento.Acierto)
        assertEquals(listOf("A" to true), progreso.intentosRegistrados)
    }

    @Test
    fun `una letra distinta de la objetivo se registra como fallo`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.LetraReconocida("S", 0.88f)

        repeat(2) { caso("A", frameConMano, estabilizador) }
        val confirmado = caso("A", frameConMano, estabilizador)

        assertTrue(
            (confirmado as ResultadoDeFrame.Confirmado).resultado is ResultadoIntento.OtraLetra,
        )
        assertEquals(listOf("A" to false), progreso.intentosRegistrados)
    }

    @Test
    fun `el no reconocida se estabiliza para no parpadear pero no cuenta como intento`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.SenaNoReconocida()

        repeat(2) { caso("A", frameConMano, estabilizador) }
        val resultado = caso("A", frameConMano, estabilizador)

        assertTrue(resultado is ResultadoDeFrame.NoReconocidaEstable)
        assertTrue(progreso.intentosRegistrados.isEmpty())
    }

    @Test
    fun `un frame sin mano segun el clasificador tampoco registra nada`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.ManoNoDetectada

        val resultado = caso("A", frameConMano, estabilizador)

        assertTrue(resultado is ResultadoDeFrame.Buscando)
        assertTrue(progreso.intentosRegistrados.isEmpty())
    }

    @Test
    fun `la coincidencia es la confianza cuando el modelo ve la sena objetivo`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.SenaNoReconocida("A", confianza = 0.41f)

        val resultado = caso("A", frameConMano, estabilizador)

        assertEquals(0.41f, (resultado as ResultadoDeFrame.Buscando).coincidencia, 0.0001f)
    }

    @Test
    fun `si el modelo ve otra letra la coincidencia con la objetivo es cero`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.LetraReconocida("S", 0.88f)

        val resultado = caso("A", frameConMano, estabilizador)

        assertEquals(0f, (resultado as ResultadoDeFrame.Buscando).coincidencia, 0.0001f)
    }

    @Test
    fun `el no reconocida estable conserva la coincidencia del ultimo frame`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.SenaNoReconocida("A", confianza = 0.3f)

        repeat(2) { caso("A", frameConMano, estabilizador) }
        val resultado = caso("A", frameConMano, estabilizador)

        assertEquals(0.3f, (resultado as ResultadoDeFrame.NoReconocidaEstable).coincidencia, 0.0001f)
    }

    @Test
    fun `promedia la coincidencia mostrada en los frames sucesivos`() = runTest {
        clasificador.siguienteResultado = ResultadoClasificacion.SenaNoReconocida("A", confianza = 0.40f)
        caso("A", frameConMano, estabilizador)

        clasificador.siguienteResultado = ResultadoClasificacion.SenaNoReconocida("A", confianza = 0.60f)
        val segundo = caso("A", frameConMano, estabilizador)

        // Promedio de 0.40 y 0.60 es 0.50
        assertEquals(0.50f, (segundo as ResultadoDeFrame.Buscando).coincidencia, 0.0001f)
    }
}
