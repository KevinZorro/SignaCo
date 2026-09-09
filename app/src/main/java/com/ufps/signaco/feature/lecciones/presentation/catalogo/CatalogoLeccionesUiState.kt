package com.ufps.signaco.feature.lecciones.presentation.catalogo

import com.ufps.signaco.feature.lecciones.domain.model.CategoriaConLecciones

/**
 * Estado único de la pantalla de catálogo. Carga y error son campos explícitos: la pantalla
 * nunca tiene que deducirlos de una lista vacía.
 */
data class CatalogoLeccionesUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val categorias: List<CategoriaConLecciones> = emptyList(),
)
