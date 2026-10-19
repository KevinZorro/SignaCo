package com.ufps.signaco.feature.practica.domain.usecase

import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.feature.practica.domain.model.PasoTrasElAcierto
import javax.inject.Inject

/**
 * Decide a dónde lleva el botón que aparece tras acertar.
 *
 * - Letra suelta (se tocó una letra en la lista): siempre de vuelta a la lección, sin seguir
 *   con las demás.
 * - Lección completa («Empezar práctica»): a la siguiente letra practicable en el orden de la
 *   lección, y tras la última, al cierre.
 *
 * @param senas las señas practicables de la lección, en orden.
 */
class DecidirPasoTrasElAciertoUseCase @Inject constructor() {

    operator fun invoke(
        senas: List<Sena>,
        senaActualId: String,
        soloEstaSena: Boolean,
    ): PasoTrasElAcierto {
        if (soloEstaSena) return PasoTrasElAcierto.VolverALaLeccion
        val indice = senas.indexOfFirst { it.id == senaActualId }
        val siguiente = senas.getOrNull(indice + 1)
        return if (indice >= 0 && siguiente != null) {
            PasoTrasElAcierto.SiguienteSena(siguiente.id)
        } else {
            PasoTrasElAcierto.TerminarLeccion
        }
    }
}
