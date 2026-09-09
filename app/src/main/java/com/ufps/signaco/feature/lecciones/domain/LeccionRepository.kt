package com.ufps.signaco.feature.lecciones.domain

import com.ufps.signaco.feature.lecciones.domain.model.Leccion
import kotlinx.coroutines.flow.Flow

interface LeccionRepository {
    fun observarLecciones(): Flow<List<Leccion>>
    fun observarLeccion(leccionId: String): Flow<Leccion?>
}
