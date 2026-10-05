package com.ufps.signaco.core.reconocimiento.data

import com.ufps.signaco.core.comun.DispatcherDefault
import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import com.ufps.signaco.core.reconocimiento.domain.SignClassifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Clasificador de mentira para construir la UI sin el modelo.
 *
 * Interpreta los frames que fabrica [LandmarksSimulados]:
 * - arreglo vacio o de tamano distinto de 63 -> [ResultadoClasificacion.ManoNoDetectada]
 * - frame marcado como no reconocido      -> [ResultadoClasificacion.SenaNoReconocida]
 * - frame que codifica una letra          -> [ResultadoClasificacion.LetraReconocida]
 *
 * La confianza es deterministica por letra: la misma letra siempre devuelve el mismo valor,
 * asi las pruebas y la UI no parpadean con numeros distintos en cada llamada.
 *
 * El retardo imita el costo real de HandLandmarker + TFLite (unas decenas de milisegundos)
 * para que la UI se disene contra un tiempo de respuesta parecido al de produccion.
 */
@Singleton
class FakeSignClassifier @Inject constructor(
    @DispatcherDefault private val dispatcher: CoroutineDispatcher,
) : SignClassifier {

    override suspend fun clasificar(landmarks: FloatArray): ResultadoClasificacion =
        withContext(dispatcher) {
            delay(RETARDO_SIMULADO_MS)
            if (landmarks.size != LandmarksSimulados.VALORES) {
                return@withContext ResultadoClasificacion.ManoNoDetectada
            }
            val letra = LandmarksSimulados.letraCodificadaEn(landmarks)
                ?: return@withContext ResultadoClasificacion.SenaNoReconocida()
            ResultadoClasificacion.LetraReconocida(letra, confianzaSimuladaDe(letra))
        }

    private fun confianzaSimuladaDe(letra: String): Float {
        val variacion = abs(letra.hashCode()) % 16
        return CONFIANZA_BASE + variacion / 100f
    }

    private companion object {
        const val RETARDO_SIMULADO_MS = 35L
        const val CONFIANZA_BASE = 0.82f
    }
}
