package com.ufps.signaco.feature.progreso.domain.model

/**
 * Intentos acumulados sobre una seña.
 *
 * La regla de "dominada" vive aquí, en el dominio: una seña se considera dominada cuando se
 * acertó al menos [ACIERTOS_PARA_DOMINAR] veces y la tasa de acierto llega a
 * [TASA_MINIMA_PARA_DOMINAR]. Los dos criterios juntos evitan que una seña quede dominada
 * por suerte en pocos intentos.
 */
data class ProgresoSena(
    val senaId: String,
    val intentos: Int,
    val aciertos: Int,
) {
    val tasaDeAcierto: Float
        get() = if (intentos == 0) 0f else aciertos.toFloat() / intentos

    val dominada: Boolean
        get() = aciertos >= ACIERTOS_PARA_DOMINAR && tasaDeAcierto >= TASA_MINIMA_PARA_DOMINAR

    companion object {
        const val ACIERTOS_PARA_DOMINAR = 5
        const val TASA_MINIMA_PARA_DOMINAR = 0.6f

        fun vacio(senaId: String) = ProgresoSena(senaId = senaId, intentos = 0, aciertos = 0)
    }
}
