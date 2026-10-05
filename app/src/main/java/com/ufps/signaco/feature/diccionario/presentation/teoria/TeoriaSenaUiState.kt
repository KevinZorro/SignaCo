package com.ufps.signaco.feature.diccionario.presentation.teoria

import com.ufps.signaco.core.senas.domain.model.Sena

data class TeoriaSenaUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val sena: Sena? = null,
    /** Lección donde se practica la seña. null si la seña no se practica en la app. */
    val leccionId: String? = null,
) {
    val noEncontrada: Boolean get() = !cargando && !hayError && sena == null
}
