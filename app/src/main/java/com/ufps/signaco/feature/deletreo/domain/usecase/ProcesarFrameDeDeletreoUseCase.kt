package com.ufps.signaco.feature.deletreo.domain.usecase

import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import com.ufps.signaco.core.reconocimiento.domain.SignClassifier
import com.ufps.signaco.feature.deletreo.domain.EventoDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.FrameDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.MotorDeDeletreo
import com.ufps.signaco.feature.practica.domain.model.FrameDeLandmarks
import javax.inject.Inject

/**
 * Lleva un frame de la cámara al motor de deletreo: clasifica (el umbral ya lo aplica el
 * clasificador) y traduce el resultado a uno de los tres estados que el motor entiende.
 * A diferencia de la práctica, aquí NO hay seña objetivo ni pestillo por mano.
 */
class ProcesarFrameDeDeletreoUseCase @Inject constructor(
    private val clasificador: SignClassifier,
) {
    suspend operator fun invoke(frame: FrameDeLandmarks, motor: MotorDeDeletreo): List<EventoDeDeletreo> {
        val t = frame.timestampMs
        if (!frame.hayMano) return motor.registrar(FrameDeDeletreo.SinMano(t))
        val entrada = when (val r = clasificador.clasificar(frame.landmarks)) {
            is ResultadoClasificacion.LetraReconocida -> FrameDeDeletreo.Letra(t, r.letra, r.confianza)
            is ResultadoClasificacion.SenaNoReconocida -> FrameDeDeletreo.Rechazada(t)
            ResultadoClasificacion.ManoNoDetectada -> FrameDeDeletreo.SinMano(t)
        }
        return motor.registrar(entrada)
    }
}
