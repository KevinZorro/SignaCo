package com.ufps.signaco.feature.lecciones.data

import com.ufps.signaco.core.database.dao.LeccionDao
import com.ufps.signaco.core.database.entity.LeccionConSenas
import com.ufps.signaco.core.senas.data.aDominio
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import com.ufps.signaco.feature.lecciones.domain.model.Leccion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LeccionRepositoryRoom @Inject constructor(
    private val leccionDao: LeccionDao,
) : LeccionRepository {

    override fun observarLecciones(): Flow<List<Leccion>> =
        leccionDao.observarLeccionesConSenas().map { filas -> filas.map { it.aDominio() } }

    override fun observarLeccion(leccionId: String): Flow<Leccion?> =
        leccionDao.observarLeccionConSenas(leccionId).map { it?.aDominio() }
}

/**
 * La relación con Junction de Room no garantiza el orden de las señas, así que se ordenan
 * aquí por el campo `orden` del catálogo.
 */
private fun LeccionConSenas.aDominio() = Leccion(
    id = leccion.id,
    titulo = leccion.titulo,
    descripcion = leccion.descripcion,
    categoria = runCatching { Categoria.valueOf(leccion.categoria) }
        .getOrDefault(Categoria.CONSONANTES),
    orden = leccion.orden,
    senas = senas.sortedBy { it.orden }.map { it.aDominio() },
)
