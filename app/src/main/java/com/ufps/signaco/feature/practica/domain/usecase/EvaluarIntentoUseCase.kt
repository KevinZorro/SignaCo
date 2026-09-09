package com.ufps.signaco.feature.practica.domain.usecase

import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import com.ufps.signaco.core.reconocimiento.domain.SignClassifier
import com.ufps.signaco.feature.practica.domain.model.ResultadoIntento
import com.ufps.signaco.feature.progreso.domain.usecase.RegistrarIntentoUseCase
import javax.inject.Inject

/**
 * Clasifica un intento, lo compara con la seña que se está practicando y lo registra.
 *
 * Se llama una vez por intento, no una vez por frame. Cuando entre la cámara, quien decida
 * que un conjunto de frames constituye un intento será la lógica de estabilidad temporal
 * (parámetro k), no este caso de uso.
 *
 * [senaObjetivo] es el identificador de la seña, que es exactamente la etiqueta que
 * devuelve el clasificador ("A".."Z", "NN"). No es [com.ufps.signaco.core.senas.domain.model.Sena.letra],
 * que es lo que se le muestra al usuario y para la enie no coincide.
 *
 * No aplica ningún umbral de confianza: eso vive en config_rechazo.json y lo resuelve el
 * clasificador. Si llega una letra reconocida, ya pasó el umbral.
 */
class EvaluarIntentoUseCase @Inject constructor(
    private val clasificador: SignClassifier,
    private val registrarIntento: RegistrarIntentoUseCase,
) {
    suspend operator fun invoke(senaObjetivo: String, landmarks: FloatArray): ResultadoIntento =
        when (val clasificacion = clasificador.clasificar(landmarks)) {
            is ResultadoClasificacion.ManoNoDetectada -> {
                // Sin mano no hubo intento: no se registra nada.
                ResultadoIntento.SinMano
            }

            is ResultadoClasificacion.SenaNoReconocida -> {
                registrarIntento(senaObjetivo, acerto = false)
                ResultadoIntento.NoReconocida
            }

            is ResultadoClasificacion.LetraReconocida -> {
                val acerto = clasificacion.letra == senaObjetivo
                registrarIntento(senaObjetivo, acerto)
                if (acerto) {
                    ResultadoIntento.Acierto(clasificacion.letra, clasificacion.confianza)
                } else {
                    ResultadoIntento.OtraLetra(clasificacion.letra, clasificacion.confianza)
                }
            }
        }
}
