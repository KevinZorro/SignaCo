package com.ufps.signaco.feature.deletreo.domain

/**
 * Parámetros del deletreo. Viven en assets/config_deletreo.json (copia de configs/spelling.json
 * del repo del modelo) y no en el código, igual que los umbrales: se ajustan sin recompilar.
 *
 * Los valores por defecto son los de la referencia en Python (lsc70/spelling.py).
 */
data class ConfiguracionDeDeletreo(
    /** Tiempo que hay que sostener una letra para confirmarla. */
    val sostenerMs: Long = 1000,
    /** Mínimo de frames dentro de ese tiempo, para que un hueco de cámara no confirme. */
    val minimoDeFrames: Int = 3,
    /** Sin letra durante este tiempo se "suelta": la MISMA letra puede escribirse otra vez (LL, RR). */
    val soltarMs: Long = 200,
    /** Los frames de los primeros ms de una mano que aparece se ignoran: aún se está acomodando. */
    val guardaDeEntradaMs: Long = 300,
    /** Sin letra durante este tiempo se da por terminada la palabra. */
    val pausaDePalabraMs: Long = 1500,
    val maximoDeLetras: Int = 20,
    /** Letras dinámicas: el modelo estático no las deletrea, así que sus palabras no entran. */
    val letrasExcluidas: Set<String> = setOf("G", "H", "J", "S", "Z", "NN"),
    /** Sustituir una letra observada cuesta su confianza, nunca menos que esto. */
    val costoMinimoDeSustitucion: Float = 0.35f,
    /** Borrar la PRIMERA o ÚLTIMA letra observada cuesta esto: subir y bajar la mano las agrega. */
    val costoDeBorrarEnElBorde: Float = 0.4f,
    /** Costo máximo aceptado según la cantidad de letras; la clave "default" cubre el resto. */
    val costoMaximoPorLongitud: Map<String, Float> = mapOf(
        "1" to 0f, "2" to 0f, "3" to 1f, "4" to 1f, "5" to 1f, "6" to 1.5f, "7" to 1.5f,
        "default" to 2f,
    ),
    val margenDeAmbiguedad: Float = 0.25f,
    val maximoDeCandidatas: Int = 3,
    val gestos: ConfiguracionDeGestos = ConfiguracionDeGestos(),
) {
    fun costoMaximo(letras: Int): Float =
        costoMaximoPorLongitud[letras.toString()] ?: costoMaximoPorLongitud.getValue("default")
}
