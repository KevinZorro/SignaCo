package com.ufps.signaco.feature.practica.domain.usecase

import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import com.ufps.signaco.core.reconocimiento.domain.SignClassifier
import com.ufps.signaco.feature.practica.domain.EstabilizadorDePredicciones
import com.ufps.signaco.feature.practica.domain.model.EstadoDeReconocimiento
import com.ufps.signaco.feature.practica.domain.model.FrameDeLandmarks
import com.ufps.signaco.feature.practica.domain.model.ResultadoDeFrame
import javax.inject.Inject

/**
 * Convierte el chorro de frames de la cámara en intentos.
 *
 * Es la pieza que faltaba entre ambos mundos: la cámara produce 15-30 frames por segundo, pero
 * [EvaluarIntentoUseCase] se llama UNA VEZ POR INTENTO porque registra progreso. Quien decide
 * que un conjunto de frames constituye un intento es el suavizado temporal.
 *
 * Sobre la doble clasificación: al confirmarse, el frame se vuelve a clasificar dentro de
 * [EvaluarIntentoUseCase]. Es a propósito, para no tocar ese caso de uso. Cuesta una inferencia
 * extra POR INTENTO CONFIRMADO, no por frame, y como la clasificación es determinista sobre la
 * misma entrada, el resultado es idéntico.
 */
class ProcesarFrameUseCase @Inject constructor(
    private val clasificador: SignClassifier,
    private val evaluarIntento: EvaluarIntentoUseCase,
) {
    suspend operator fun invoke(
        senaObjetivo: String,
        frame: FrameDeLandmarks,
        estabilizador: EstabilizadorDePredicciones,
    ): ResultadoDeFrame {
        if (!frame.hayMano) {
            estabilizador.reiniciar()
            return ResultadoDeFrame.Buscando(
                estado = EstadoDeReconocimiento.BUSCANDO_MANOS,
                avance = 0,
                total = estabilizador.k,
            )
        }

        val clasificacion = clasificador.clasificar(frame.landmarks)
        val clave = when (clasificacion) {
            is ResultadoClasificacion.LetraReconocida -> clasificacion.letra
            is ResultadoClasificacion.SenaNoReconocida ->
                EstabilizadorDePredicciones.CLAVE_NO_RECONOCIDA
            ResultadoClasificacion.ManoNoDetectada -> {
                estabilizador.reiniciar()
                return ResultadoDeFrame.Buscando(
                    estado = EstadoDeReconocimiento.BUSCANDO_MANOS,
                    avance = 0,
                    total = estabilizador.k,
                )
            }
        }
        val coincidencia = coincidenciaCon(senaObjetivo, clasificacion)

        val confirmada = estabilizador.registrar(clave)
            ?: return ResultadoDeFrame.Buscando(
                estado = EstadoDeReconocimiento.RECONOCIENDO,
                avance = estabilizador.avance,
                total = estabilizador.k,
                coincidencia = coincidencia,
            )

        if (confirmada == EstabilizadorDePredicciones.CLAVE_NO_RECONOCIDA) {
            return ResultadoDeFrame.NoReconocidaEstable(coincidencia)
        }

        return ResultadoDeFrame.Confirmado(evaluarIntento(senaObjetivo, frame.landmarks))
    }

    /**
     * Cuánto se parece el frame a la seña que se practica. Si el modelo ve otra letra, la
     * coincidencia con la objetivo es cero: su probabilidad no está en el resultado.
     */
    private fun coincidenciaCon(senaObjetivo: String, clasificacion: ResultadoClasificacion): Float =
        when (clasificacion) {
            is ResultadoClasificacion.LetraReconocida ->
                if (clasificacion.letra == senaObjetivo) clasificacion.confianza else 0f
            is ResultadoClasificacion.SenaNoReconocida ->
                if (clasificacion.candidata == senaObjetivo) clasificacion.confianza else 0f
            ResultadoClasificacion.ManoNoDetectada -> 0f
        }
}
