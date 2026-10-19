package com.ufps.signaco.feature.practica.domain.model

/**
 * Qué ofrece la práctica después de acertar una seña. Depende de cómo se entró a practicar:
 * recorriendo toda la lección o una sola letra elegida de la lista.
 */
sealed interface PasoTrasElAcierto {

    /** Recorriendo la lección y quedan letras: se pasa a [senaId]. */
    data class SiguienteSena(val senaId: String) : PasoTrasElAcierto

    /** Recorriendo la lección y era la última letra: se cierra con el quiz. */
    data object TerminarLeccion : PasoTrasElAcierto

    /** Se practicaba una letra suelta: se vuelve a la lista de la lección. */
    data object VolverALaLeccion : PasoTrasElAcierto
}
