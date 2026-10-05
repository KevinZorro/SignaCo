package com.ufps.signaco.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Espaciado en escala de 4 dp. Elementos equivalentes usan el mismo paso en todas las
 * pantallas; no se escriben dp sueltos en los Composables.
 */
object Espacio {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp

    /** Margen lateral de todas las pantallas. */
    val margen = 20.dp
}

object Medida {
    val alturaDeBoton = 56.dp

    /** Área táctil mínima (RNF-11). */
    val areaTactil = 48.dp
    val icono = 24.dp
    val miniatura = 72.dp
    val cuadroDeProgreso = 16.dp
    val cuadroDelCuaderno = 24.dp
    val segmentoDeProgreso = 6.dp
    val ladoDeCuadricula = 16.dp
    val trazoFino = 1.dp
}

object Forma {
    val pieza = RoundedCornerShape(8.dp)

    /** Cuadro del cuaderno: con 8 dp un cuadro de 16 dp sería un círculo y dejaría de leerse como cuadro. */
    val cuadro = RoundedCornerShape(4.dp)
    val contenedor = RoundedCornerShape(16.dp)
    val contenedorGrande = RoundedCornerShape(24.dp)
    val pildora = RoundedCornerShape(percent = 50)
}

/** Movimiento corto y con propósito: 150 a 250 ms, con desaceleración y sin rebotes. */
object Movimiento {
    const val corto = 150
    const val medio = 200
    const val largo = 250
}
