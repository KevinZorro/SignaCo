package com.ufps.signaco.feature.progreso.domain.model

/**
 * Días consecutivos con actividad.
 *
 * @param ultimoDiaActivo día de la última actividad, en días desde 1970-01-01.
 *        Vale [SIN_ACTIVIDAD] cuando el usuario todavía no ha practicado nunca.
 */
data class Racha(
    val diasConsecutivos: Int,
    val mejorRacha: Int,
    val ultimoDiaActivo: Long,
) {
    companion object {
        const val SIN_ACTIVIDAD = Long.MIN_VALUE

        val INICIAL = Racha(
            diasConsecutivos = 0,
            mejorRacha = 0,
            ultimoDiaActivo = SIN_ACTIVIDAD,
        )
    }
}
