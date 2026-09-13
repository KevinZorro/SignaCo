package com.ufps.signaco.feature.practica.domain.model

/** Lo que sale de procesar un frame de cámara. */
sealed interface ResultadoDeFrame {

    /** Todavía no hay k frames iguales. La pantalla muestra el indicador y el avance. */
    data class Buscando(
        val estado: EstadoDeReconocimiento,
        val avance: Int,
        val total: Int,
    ) : ResultadoDeFrame

    /**
     * Se estabilizó "no reconocida". Se le muestra al usuario, pero NO cuenta como intento:
     * a 30 fps registrar uno cada k frames mientras titubea le arruinaría las estadísticas.
     */
    data object NoReconocidaEstable : ResultadoDeFrame

    /** Se estabilizó una letra. Esto sí es un intento, y ya quedó registrado. */
    data class Confirmado(val resultado: ResultadoIntento) : ResultadoDeFrame
}
