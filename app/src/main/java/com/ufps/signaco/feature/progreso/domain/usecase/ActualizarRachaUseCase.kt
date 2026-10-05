package com.ufps.signaco.feature.progreso.domain.usecase

import com.ufps.signaco.core.comun.Reloj
import com.ufps.signaco.feature.progreso.domain.RachaRepository
import com.ufps.signaco.feature.progreso.domain.model.Racha
import javax.inject.Inject
import kotlin.math.max

/**
 * Registra que hoy hubo actividad y ajusta la racha.
 *
 * Reglas:
 * - Si ya se registró actividad hoy, no cambia nada (es idempotente dentro del mismo día).
 * - Si la última actividad fue ayer, la racha suma uno.
 * - En cualquier otro caso (primer día, o se saltó uno o más días), la racha vuelve a uno.
 * - La mejor racha nunca baja.
 */
class ActualizarRachaUseCase @Inject constructor(
    private val rachaRepository: RachaRepository,
    private val reloj: Reloj,
) {
    suspend operator fun invoke(): Racha {
        val hoy = reloj.hoyEnDias()
        val actual = rachaRepository.obtenerRacha()
        if (actual.ultimoDiaActivo == hoy) return actual

        rachaRepository.registrarDiaActivo(hoy)
        val dias = if (actual.ultimoDiaActivo == hoy - 1) actual.diasConsecutivos + 1 else 1
        val actualizada = Racha(
            diasConsecutivos = dias,
            mejorRacha = max(actual.mejorRacha, dias),
            ultimoDiaActivo = hoy,
        )
        rachaRepository.guardarRacha(actualizada)
        return actualizada
    }
}
