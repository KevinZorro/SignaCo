package com.ufps.signaco.core.senas.data

import com.ufps.signaco.core.database.entity.SenaEntity
import com.ufps.signaco.core.senas.domain.ReferenciasVisuales
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena

/**
 * Traduce de la tabla al modelo de dominio. El dominio no conoce Room.
 *
 * La referencia visual no sale de la base: se busca por identificador en [referencias].
 */
fun SenaEntity.aDominio(referencias: ReferenciasVisuales): Sena = Sena(
    id = id,
    letra = letra,
    nombre = nombre,
    categoria = runCatching { Categoria.valueOf(categoria) }.getOrDefault(Categoria.CONSONANTES),
    orden = orden,
    disponible = disponible,
    referencia = referencias.de(id),
)
