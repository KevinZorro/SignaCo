package com.ufps.signaco.feature.progreso.domain

import com.ufps.signaco.feature.progreso.domain.model.Racha
import kotlinx.coroutines.flow.Flow

interface RachaRepository {
    fun observarRacha(): Flow<Racha>
    suspend fun obtenerRacha(): Racha
    suspend fun guardarRacha(racha: Racha)

    /** Marca [dia] (días desde 1970-01-01) como día con actividad. Repetirlo no cambia nada. */
    suspend fun registrarDiaActivo(dia: Long)

    /** Días con actividad desde [dia], inclusive. */
    fun observarDiasActivosDesde(dia: Long): Flow<Set<Long>>
}
