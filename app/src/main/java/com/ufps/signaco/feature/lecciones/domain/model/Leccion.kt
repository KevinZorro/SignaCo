package com.ufps.signaco.feature.lecciones.domain.model

import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena

/** Una lección del catálogo con las señas que la componen, en orden. */
data class Leccion(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: Categoria,
    val orden: Int,
    val senas: List<Sena>,
)
