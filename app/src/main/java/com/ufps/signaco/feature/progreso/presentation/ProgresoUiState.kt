package com.ufps.signaco.feature.progreso.presentation

import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso
import com.ufps.signaco.feature.progreso.domain.model.ResumenProgreso

data class ProgresoUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val resumen: ResumenProgreso = ResumenProgreso.VACIO,
    /** Una fila del cuaderno por lección, en el orden del catálogo. */
    val lecciones: List<LeccionConProgreso> = emptyList(),
    val borrando: Boolean = false,
)
