package com.ufps.signaco.feature.diccionario.presentation

import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena

data class DiccionarioUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val consulta: String = "",
    /** null es «Todos». */
    val categoria: Categoria? = null,
    val senas: List<Sena> = emptyList(),
) {
    val sinResultados: Boolean get() = !cargando && !hayError && senas.isEmpty()
}
