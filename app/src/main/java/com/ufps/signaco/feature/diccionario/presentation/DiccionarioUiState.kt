package com.ufps.signaco.feature.diccionario.presentation

import com.ufps.signaco.core.senas.domain.model.Sena

data class DiccionarioUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val consulta: String = "",
    val senas: List<Sena> = emptyList(),
) {
    val sinResultados: Boolean get() = !cargando && !hayError && senas.isEmpty()
}
