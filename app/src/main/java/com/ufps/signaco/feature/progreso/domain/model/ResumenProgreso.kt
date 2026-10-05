package com.ufps.signaco.feature.progreso.domain.model

import com.ufps.signaco.core.senas.domain.model.Sena

/** Una seña con lo que el usuario lleva practicado de ella. */
data class AvanceDeSena(
    val sena: Sena,
    val progreso: ProgresoSena,
)

/** Lo que muestra la pantalla de progreso, ya calculado en el dominio. */
data class ResumenProgreso(
    val senasDominadas: Int,
    val senasDisponibles: Int,
    val leccionesCompletadas: Int,
    val rachaActual: Int,
    val mejorRacha: Int,
    val avancePorSena: List<AvanceDeSena>,
    /** Siete valores, de lunes a domingo de la semana actual: true si ese día hubo actividad. */
    val actividadDeLaSemana: List<Boolean> = List(DIAS_DE_LA_SEMANA) { false },
) {
    /** Entre 0f y 1f. Sirve directo para la barra de progreso. */
    val porcentajeDominado: Float
        get() = if (senasDisponibles == 0) 0f else senasDominadas.toFloat() / senasDisponibles

    companion object {
        const val DIAS_DE_LA_SEMANA = 7

        /**
         * Lunes de la semana de [dia] (días desde 1970-01-01). El 1 de enero de 1970 fue
         * jueves, de ahí el desfase de 3.
         */
        fun lunesDeLaSemana(dia: Long): Long = dia - Math.floorMod(dia + 3, DIAS_DE_LA_SEMANA.toLong())

        val VACIO = ResumenProgreso(
            senasDominadas = 0,
            senasDisponibles = 0,
            leccionesCompletadas = 0,
            rachaActual = 0,
            mejorRacha = 0,
            avancePorSena = emptyList(),
        )
    }
}
