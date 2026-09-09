package com.ufps.signaco.feature.progreso.presentation

import com.ufps.signaco.feature.progreso.domain.model.ResumenProgreso

data class ProgresoUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val resumen: ResumenProgreso = ResumenProgreso.VACIO,
)
