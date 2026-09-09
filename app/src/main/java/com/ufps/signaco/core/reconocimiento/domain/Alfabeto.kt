package com.ufps.signaco.core.reconocimiento.domain

/**
 * Vocabulario de letras que maneja el clasificador, en el mismo orden que las etiquetas
 * del modelo. La enie ("NN") esta en la lista porque el modelo la contempla, pero esta
 * marcada como no disponible en el catalogo: fuera del MVP.
 */
object Alfabeto {

    val LETRAS: List<String> = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "NN", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
    )

    /** Etiqueta de la clase de rechazo del modelo. No es una letra. */
    const val NO_ES_SENA = "no_es_sena"

    fun indiceDe(letra: String): Int = LETRAS.indexOf(letra)

    fun letraEn(indice: Int): String? = LETRAS.getOrNull(indice)
}
