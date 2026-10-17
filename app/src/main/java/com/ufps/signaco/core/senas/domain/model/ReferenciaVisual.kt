package com.ufps.signaco.core.senas.domain.model

/**
 * Material para ver cómo se hace una seña (RF02).
 *
 * Es una interfaz sellada para que mañana entren una foto o un video como casos nuevos sin
 * rehacer el dominio: quien la usa decide qué dibujar según el caso, y una seña sin material
 * simplemente no tiene referencia (null).
 */
sealed interface ReferenciaVisual {

    /**
     * Imagen fija empaquetada con la app.
     *
     * @param recurso identificador del recurso de Android (R.drawable.sena_a). Es un Int a
     *        propósito: así domain no importa nada de Android.
     */
    data class Imagen(val recurso: Int) : ReferenciaVisual
}
