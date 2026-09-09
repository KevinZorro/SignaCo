package com.ufps.signaco.feature.lecciones.domain.usecase

import com.ufps.signaco.feature.lecciones.domain.model.Leccion
import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso
import com.ufps.signaco.feature.lecciones.domain.model.SenaConProgreso
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import javax.inject.Inject

/**
 * Pega una lección con el progreso del usuario. Lo usan el catálogo y el detalle, así que
 * la regla de cruce está en un solo lugar.
 */
class CombinarLeccionConProgresoUseCase @Inject constructor() {

    operator fun invoke(
        leccion: Leccion,
        progreso: List<ProgresoSena>,
        leccionesCompletadas: Set<String>,
    ): LeccionConProgreso {
        val progresoPorSena = progreso.associateBy { it.senaId }
        return LeccionConProgreso(
            leccion = leccion,
            senas = leccion.senas.map { sena ->
                SenaConProgreso(
                    sena = sena,
                    progreso = progresoPorSena[sena.id] ?: ProgresoSena.vacio(sena.id),
                )
            },
            completada = leccion.id in leccionesCompletadas,
        )
    }
}
