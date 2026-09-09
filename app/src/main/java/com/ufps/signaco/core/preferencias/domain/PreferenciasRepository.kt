package com.ufps.signaco.core.preferencias.domain

import kotlinx.coroutines.flow.Flow

interface PreferenciasRepository {
    fun observarPreferencias(): Flow<PreferenciasUsuario>
    suspend fun cambiarMostrarPistas(activo: Boolean)
    suspend fun cambiarRetroalimentacionHaptica(activo: Boolean)
}
