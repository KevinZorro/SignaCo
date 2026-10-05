package com.ufps.signaco.feature.lecciones.domain.usecase

import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import com.ufps.signaco.feature.lecciones.domain.model.CategoriaConLecciones
import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso
import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Catálogo completo agrupado por categoría, con el avance de cada lección.
 *
 * Cruza el catálogo con el progreso guardado. El cruce y el conteo son reglas del dominio,
 * no de la pantalla: el ViewModel recibe esto ya resuelto.
 *
 * Marca una sola lección en curso: la primera sin completar que ya tenga algún intento; si no
 * hay ninguna empezada, la primera sin completar. Si todas están completas, ninguna.
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
        val combinadas = lecciones
            .map { combinarProgreso(it, progreso, completadas) }
            .sortedWith(compareBy({ it.leccion.categoria.ordinal }, { it.leccion.orden }))
        val enCurso = leccionEnCurso(combinadas)
        combinadas
            .map { if (it.leccion.id == enCurso) it.copy(enCurso = true) else it }
            .groupBy { it.leccion.categoria }
            .map { (categoria, deLaCategoria) ->
                CategoriaConLecciones(
                    categoria = categoria,
                    lecciones = deLaCategoria.sortedBy { it.leccion.orden },
                )
            }
            .sortedBy { it.categoria.ordinal }
    }

    private fun leccionEnCurso(lecciones: List<LeccionConProgreso>): String? {
        val pendientes = lecciones.filterNot { it.completada }
        val empezada = pendientes.firstOrNull { leccion -> leccion.senas.any { it.progreso.intentos > 0 } }
        return (empezada ?: pendientes.firstOrNull())?.leccion?.id
    }
}
