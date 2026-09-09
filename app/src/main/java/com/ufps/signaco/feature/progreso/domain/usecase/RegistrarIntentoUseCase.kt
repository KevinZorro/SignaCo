package com.ufps.signaco.feature.progreso.domain.usecase

import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import javax.inject.Inject

/**
 * Guarda el resultado de un intento y marca el día como activo.
 *
 * Se llama una vez por intento del usuario, no una vez por frame de cámara: cuando se porte
 * la captura, quien decide que hubo un intento es la lógica de estabilidad temporal
 * (parámetro k de config_rechazo.json), no cada llegada de landmarks.
 */
class RegistrarIntentoUseCase @Inject constructor(
    private val progresoRepository: ProgresoRepository,
    private val actualizarRacha: ActualizarRachaUseCase,
) {
    suspend operator fun invoke(senaId: String, acerto: Boolean) {
        progresoRepository.registrarIntento(senaId, acerto)
        actualizarRacha()
    }
}
