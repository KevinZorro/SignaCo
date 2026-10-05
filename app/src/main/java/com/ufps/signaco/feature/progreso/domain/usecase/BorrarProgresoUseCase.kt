package com.ufps.signaco.feature.progreso.domain.usecase

import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import javax.inject.Inject

/**
 * Deja la app como recién instalada en lo que toca al usuario: intentos, lecciones aprobadas,
 * racha y días activos. El catálogo de señas y lecciones no se toca. La pantalla pide
 * confirmación antes de llamarlo, porque no se puede deshacer.
 */
class BorrarProgresoUseCase @Inject constructor(
    private val progresoRepository: ProgresoRepository,
) {
    suspend operator fun invoke() {
        progresoRepository.borrarTodo()
    }
}
