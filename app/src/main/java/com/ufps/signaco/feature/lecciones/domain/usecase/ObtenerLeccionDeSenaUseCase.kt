package com.ufps.signaco.feature.lecciones.domain.usecase

import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * La lección donde se practica una seña, para poder ir a practicarla desde el diccionario.
 * Emite null si la seña no está en ninguna lección, que es el caso de las que quedaron fuera
 * del vocabulario del MVP.
 */
class ObtenerLeccionDeSenaUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
) {
    operator fun invoke(senaId: String): Flow<String?> =
        leccionRepository.observarLecciones().map { lecciones ->
            lecciones
                .sortedBy { it.orden }
                .firstOrNull { leccion -> leccion.senas.any { it.id == senaId && it.disponible } }
                ?.id
        }
}
