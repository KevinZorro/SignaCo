package com.ufps.signaco.core.reconocimiento.domain

/**
 * Lo que trae config_rechazo.json. Los umbrales y la ventana de estabilidad viven ahí y no en
 * el código, así que se pueden ajustar sin reentrenar ni recompilar.
 *
 * El modelo emite [totalDeClases] valores: los índices 0..etiquetas.lastIndex son letras y
 * [indiceDeRechazo] es la clase `no_es_seña`.
 */
data class ConfiguracionDeReconocimiento(
    val etiquetas: List<String>,
    val indiceDeRechazo: Int,
    val umbralPorLetra: Map<String, Float>,
    val umbralGlobal: Float,
    val k: Int,
    /** De dónde salió esta configuración, para poder decirlo en el log. */
    val procedencia: String = "desconocida",
) {
    val totalDeClases: Int get() = maxOf(etiquetas.size, indiceDeRechazo + 1)

    fun letraEn(indice: Int): String? = etiquetas.getOrNull(indice)

    fun umbralDe(letra: String): Float = umbralPorLetra[letra] ?: umbralGlobal

    /** Para que el log pueda responder "¿contra qué umbral se comparó y de dónde salió?". */
    fun origenDelUmbralDe(letra: String): String =
        if (umbralPorLetra.containsKey(letra)) {
            "umbral_por_letra[\"$letra\"]"
        } else {
            "umbral_global_fallback (no hay entrada para \"$letra\")"
        }

    companion object {
        const val K_POR_DEFECTO = 3
        const val UMBRAL_POR_DEFECTO = 0.1f

        /** Solo si la lectura del JSON falla. El log deja constancia cuando se usa. */
        val RESPALDO = ConfiguracionDeReconocimiento(
            etiquetas = Alfabeto.LETRAS,
            indiceDeRechazo = Alfabeto.LETRAS.size,
            umbralPorLetra = emptyMap(),
            umbralGlobal = UMBRAL_POR_DEFECTO,
            k = K_POR_DEFECTO,
            procedencia = "RESPALDO EN CÓDIGO",
        )
    }
}
