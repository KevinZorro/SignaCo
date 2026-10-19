package com.ufps.signaco.core.senas.domain.model

/**
 * Una letra del abecedario dactilologico de la LSC.
 *
 * @param id identificador estable, coincide con la etiqueta que devuelve el clasificador
 *           ("A".."Z" y "NN" para la enie).
 * @param disponible false para las senas que estan en el catalogo pero fuera del vocabulario
 *           del MVP. Hoy solo aplica a la enie ("NN"), excluida por variacion regional
 *           documentada en el dataset LSC70: se muestra en el diccionario pero no se
 *           practica ni entra en los quiz.
 * @param referencia imagen (o, más adelante, foto o video) de cómo se hace la seña. Null si
 *           todavía no hay material: la pantalla muestra la letra grande en su lugar.
 */
data class Sena(
    val id: String,
    val letra: String,
    val nombre: String,
    val categoria: Categoria,
    val orden: Int,
    val disponible: Boolean,
    val referencia: ReferenciaVisual? = null,
) {
    val tieneReferencia: Boolean get() = referencia != null
}
