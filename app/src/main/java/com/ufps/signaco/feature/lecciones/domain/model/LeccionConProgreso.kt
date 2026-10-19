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

/**
 * Una lección con el avance del usuario ya calculado.
 *
 * @param enCurso la lección que el catálogo propone seguir; la marca el caso de uso del catálogo.
 */
data class LeccionConProgreso(
    val leccion: Leccion,
    val senas: List<SenaConProgreso>,
    val completada: Boolean,
    val enCurso: Boolean = false,
) {
    val totalDeSenas: Int get() = senas.size

    val senasDominadas: Int get() = senas.count { it.dominada }

    /** Con la que arranca «Empezar práctica». Null si la lección no tiene señas practicables. */
    val primeraSenaParaPracticar: String?
        get() = senas.firstOrNull { it.sena.disponible }?.sena?.id

    /** Entre 0f y 1f. */
    val porcentaje: Float
        get() = if (totalDeSenas == 0) 0f else senasDominadas.toFloat() / totalDeSenas
}

/** Las lecciones agrupadas por categoría, que es como las muestra el catálogo. */
data class CategoriaConLecciones(
    val categoria: Categoria,
    val lecciones: List<LeccionConProgreso>,
)
