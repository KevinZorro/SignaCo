package com.ufps.signaco.feature.practica.domain.usecase

import com.ufps.signaco.core.reconocimiento.domain.Alfabeto
import com.ufps.signaco.core.reconocimiento.domain.LandmarksSimulados
import com.ufps.signaco.feature.practica.domain.model.ResultadoIntento
import com.ufps.signaco.feature.practica.domain.model.TipoDeSimulacion
import javax.inject.Inject

/**
 * ANDAMIO TEMPORAL. Fabrica un frame falso y lo hace pasar por [EvaluarIntentoUseCase], que
 * es exactamente el mismo camino que recorrerá el frame real de la cámara.
 *
 * Existe para que la pantalla de práctica se pueda construir y probar sin cámara ni modelo.
 * Se borra cuando se porte la captura con CameraX; [EvaluarIntentoUseCase] se queda igual.
 */
class SimularIntentoUseCase @Inject constructor(
    private val evaluarIntento: EvaluarIntentoUseCase,
) {
    suspend operator fun invoke(
        senaObjetivo: String,
        tipo: TipoDeSimulacion,
    ): ResultadoIntento {
        val landmarks = when (tipo) {
            TipoDeSimulacion.SENA_CORRECTA -> LandmarksSimulados.senaDe(senaObjetivo)
            TipoDeSimulacion.OTRA_LETRA ->
                LandmarksSimulados.senaDe(otraLetraDistintaDe(senaObjetivo))
            TipoDeSimulacion.NO_RECONOCIDA -> LandmarksSimulados.senaNoReconocida()
            TipoDeSimulacion.MANO_FUERA -> LandmarksSimulados.manoFueraDeCuadro()
        }
        return evaluarIntento(senaObjetivo, landmarks)
    }

    private fun otraLetraDistintaDe(letra: String): String =
        Alfabeto.LETRAS.firstOrNull { it != letra } ?: letra
}
