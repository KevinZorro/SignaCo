package com.ufps.signaco.feature.progreso.domain

import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import kotlinx.coroutines.flow.Flow

interface ProgresoRepository {

    fun observarProgreso(): Flow<List<ProgresoSena>>

    fun observarProgresoDeSena(senaId: String): Flow<ProgresoSena?>

    /** Suma un intento a la seña, y un acierto si [acerto]. */
    suspend fun registrarIntento(senaId: String, acerto: Boolean)

    fun observarLeccionesCompletadas(): Flow<Set<String>>

    suspend fun marcarLeccionCompletada(leccionId: String, aciertosQuiz: Int, totalQuiz: Int)
}
