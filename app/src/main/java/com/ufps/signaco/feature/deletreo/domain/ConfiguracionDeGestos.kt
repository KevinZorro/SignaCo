package com.ufps.signaco.feature.deletreo.domain

/** Umbrales de los gestos de borrar y espacio (copia de "gestures" en configs/spelling.json). */
data class ConfiguracionDeGestos(
    /** punta-a-muñeca / articulación-a-muñeca igual o mayor que esto = dedo extendido. */
    val extendidoMin: Float = 1.2f,
    /** igual o menor que esto = dedo doblado. */
    val dobladoMax: Float = 1.0f,
    /** Distancia media entre puntas vecinas; solo para la mano abierta. */
    val aperturaMin: Float = 0.25f,
    /** Punta del pulgar a la base del índice / ancho de la palma. */
    val pulgarAfueraMin: Float = 1.0f,
)
