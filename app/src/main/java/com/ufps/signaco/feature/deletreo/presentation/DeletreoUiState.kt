package com.ufps.signaco.feature.deletreo.presentation

import com.ufps.signaco.feature.deletreo.domain.ProgresoDeSostenido

/** Una palabra terminada: lo que deletreó el usuario y lo que propone el decodificador. */
data class PalabraDeletreada(
    val letras: String,
    val sugerida: String?,
    val otras: List<String>,
    val ambigua: Boolean,
)

data class DeletreoUiState(
    val permisoConcedido: Boolean? = null,
    val cargando: Boolean = true,
    val error: Boolean = false,
    val capturando: Boolean = true,
    val letrasEnCurso: String = "",
    val palabras: List<PalabraDeletreada> = emptyList(),
    /** Lo que se sostiene ahora (letra o gesto) y cuánto falta, para el indicador. */
    val sostenido: ProgresoDeSostenido = ProgresoDeSostenido(null, 0f),
) {
    /**
     * Lo que el usuario lleva escrito: las palabras ya terminadas (la sugerida de la lista, o las
     * letras tal cual si nada coincidió) y, a continuación, las letras en curso. Así la palabra
     * no desaparece al cerrarla con el gesto de espacio.
     */
    val frase: String
        get() {
            val hechas = palabras.map { it.sugerida ?: it.letras.replace(" ", "") }
            return (hechas + listOfNotNull(letrasEnCurso.replace(" ", "").ifEmpty { null })).joinToString(" ")
        }
}
