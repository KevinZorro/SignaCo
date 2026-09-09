package com.ufps.signaco.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Racha de dias consecutivos. Tabla de una sola fila: la app es de un solo usuario,
 * asi que la clave primaria es una constante.
 */
@Entity(tableName = "racha")
data class RachaEntity(
    @PrimaryKey val id: Int = FILA_UNICA,
    val diasConsecutivos: Int,
    val mejorRacha: Int,
    val ultimoDiaActivoEnDias: Long,
) {
    companion object {
        const val FILA_UNICA = 0
    }
}
