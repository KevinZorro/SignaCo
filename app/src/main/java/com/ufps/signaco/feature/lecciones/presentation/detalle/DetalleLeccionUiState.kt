package com.ufps.signaco.feature.lecciones.presentation.detalle

import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso

data class DetalleLeccionUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val leccion: LeccionConProgreso? = null,
) {
    /** Terminó de cargar, sin error, y la lección no existe. */
    val noEncontrada: Boolean get() = !cargando && !hayError && leccion == null
}
