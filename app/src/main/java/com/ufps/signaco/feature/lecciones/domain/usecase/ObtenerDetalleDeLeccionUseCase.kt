package com.ufps.signaco.feature.lecciones.domain.usecase

import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso
import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Una lección con su avance. Emite null si el identificador no existe. */
class ObtenerDetalleDeLeccionUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val progresoRepository: ProgresoRepository,
    private val combinarProgreso: CombinarLeccionConProgresoUseCase,
) {
    operator fun invoke(leccionId: String): Flow<LeccionConProgreso?> = combine(
        leccionRepository.observarLeccion(leccionId),
        progresoRepository.observarProgreso(),
        progresoRepository.observarLeccionesCompletadas(),
    ) { leccion, progreso, completadas ->
        leccion?.let { combinarProgreso(it, progreso, completadas) }
    }
}
