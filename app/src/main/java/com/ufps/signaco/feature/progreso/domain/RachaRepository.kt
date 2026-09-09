package com.ufps.signaco.feature.progreso.domain

import com.ufps.signaco.feature.progreso.domain.model.Racha
import kotlinx.coroutines.flow.Flow

interface RachaRepository {
    fun observarRacha(): Flow<Racha>
    suspend fun obtenerRacha(): Racha
    suspend fun guardarRacha(racha: Racha)
}
