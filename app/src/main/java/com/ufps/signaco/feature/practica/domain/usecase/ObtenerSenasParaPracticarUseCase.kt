package com.ufps.signaco.feature.practica.domain.usecase

import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Señas de una lección que sí se pueden practicar. Deja fuera las que están en el catálogo
 * pero no en el vocabulario del MVP.
 */
class ObtenerSenasParaPracticarUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
) {
    operator fun invoke(leccionId: String): Flow<List<Sena>> =
        leccionRepository.observarLeccion(leccionId)
            .map { leccion -> leccion?.senas.orEmpty().filter { it.disponible } }
}
