package com.ufps.signaco.feature.lecciones.domain.usecase

import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import com.ufps.signaco.feature.lecciones.domain.model.CategoriaConLecciones
import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Catálogo completo agrupado por categoría, con el avance de cada lección.
 *
 * Cruza el catálogo con el progreso guardado. El cruce y el conteo son reglas del dominio,
 * no de la pantalla: el ViewModel recibe esto ya resuelto.
 */
class ObtenerCatalogoDeLeccionesUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val progresoRepository: ProgresoRepository,
    private val combinarProgreso: CombinarLeccionConProgresoUseCase,
) {
    operator fun invoke(): Flow<List<CategoriaConLecciones>> = combine(
        leccionRepository.observarLecciones(),
        progresoRepository.observarProgreso(),
        progresoRepository.observarLeccionesCompletadas(),
    ) { lecciones, progreso, completadas ->
        lecciones
            .map { combinarProgreso(it, progreso, completadas) }
            .groupBy { it.leccion.categoria }
            .map { (categoria, deLaCategoria) ->
                CategoriaConLecciones(
                    categoria = categoria,
                    lecciones = deLaCategoria.sortedBy { it.leccion.orden },
                )
            }
            .sortedBy { it.categoria.ordinal }
    }
}
