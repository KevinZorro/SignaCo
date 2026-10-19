package com.ufps.signaco.core.senas.domain

import com.ufps.signaco.core.senas.domain.model.ReferenciaVisual

/**
 * De dónde sale la referencia visual de cada seña. La asociación es por identificador de
 * seña ("A".."Z", "NN"), no un campo de la base: agregar o cambiar una imagen no pide migración.
 */
interface ReferenciasVisuales {

    /** Null si la seña todavía no tiene material. Nunca falla. */
    fun de(senaId: String): ReferenciaVisual?
}
