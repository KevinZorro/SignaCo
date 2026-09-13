package com.ufps.signaco.feature.practica.domain.model

/**
 * Los tres indicadores que el RF09 exige mostrar en todo momento.
 *
 * - [SIN_DETECCION]: la cámara no está entregando frames (captura detenida o aún sin arrancar).
 * - [BUSCANDO_MANOS]: llegan frames pero no hay mano en cuadro.
 * - [RECONOCIENDO]: hay mano y se están acumulando frames para confirmar la seña.
 */
enum class EstadoDeReconocimiento {
    SIN_DETECCION,
    BUSCANDO_MANOS,
    RECONOCIENDO,
}
