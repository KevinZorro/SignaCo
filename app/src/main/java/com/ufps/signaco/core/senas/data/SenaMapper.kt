package com.ufps.signaco.core.senas.data

import com.ufps.signaco.core.database.entity.SenaEntity
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena

/** Traduce de la tabla al modelo de dominio. El dominio no conoce Room. */
fun SenaEntity.aDominio(): Sena = Sena(
    id = id,
    letra = letra,
    nombre = nombre,
    descripcion = descripcion,
    categoria = runCatching { Categoria.valueOf(categoria) }.getOrDefault(Categoria.CONSONANTES),
    orden = orden,
    disponible = disponible,
)
