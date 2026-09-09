package com.ufps.signaco.feature.lecciones.domain.model

import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena

/** Una seña junto con lo que el usuario lleva practicado de ella. */
data class SenaConProgreso(
    val sena: Sena,
    val progreso: ProgresoSena,
) {
    val dominada: Boolean get() = progreso.dominada
}

/** Una lección con el avance del usuario ya calculado. */
data class LeccionConProgreso(
    val leccion: Leccion,
    val senas: List<SenaConProgreso>,
    val completada: Boolean,
) {
    val totalDeSenas: Int get() = senas.size

    val senasDominadas: Int get() = senas.count { it.dominada }

    /** Entre 0f y 1f. */
    val porcentaje: Float
        get() = if (totalDeSenas == 0) 0f else senasDominadas.toFloat() / totalDeSenas
}

/** Las lecciones agrupadas por categoría, que es como las muestra el catálogo. */
data class CategoriaConLecciones(
    val categoria: Categoria,
    val lecciones: List<LeccionConProgreso>,
)
