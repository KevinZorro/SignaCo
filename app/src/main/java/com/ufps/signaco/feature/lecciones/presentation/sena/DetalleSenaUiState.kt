package com.ufps.signaco.feature.lecciones.presentation.sena

import com.ufps.signaco.core.senas.domain.model.Sena

data class DetalleSenaUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val tituloDeLeccion: String = "",
    val sena: Sena? = null,
    val subtitulos: Boolean = false,
) {
    val noEncontrada: Boolean get() = !cargando && !hayError && sena == null
}
