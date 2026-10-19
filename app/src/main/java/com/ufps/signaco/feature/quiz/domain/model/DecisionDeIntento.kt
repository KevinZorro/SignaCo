package com.ufps.signaco.feature.quiz.domain.model

/** Qué pasa con una pregunta del quiz con cámara después de un intento confirmado. */
sealed interface DecisionDeIntento {

    /** Hizo la seña: la pregunta cuenta como acertada. */
    data object Acertada : DecisionDeIntento

    /** Falló, pero le quedan [intentosRestantes] en esta misma pregunta. */
    data class OtraOportunidad(val intentosRestantes: Int) : DecisionDeIntento

    /** Gastó todos los intentos (o saltó la pregunta): cuenta como error. */
    data object Fallada : DecisionDeIntento
}
