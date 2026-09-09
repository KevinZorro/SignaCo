package com.ufps.signaco.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/** Intentos y aciertos acumulados por sena. Una fila por sena practicada. */
@Entity(
    tableName = "progreso_senas",
    foreignKeys = [
        ForeignKey(
            entity = SenaEntity::class,
            parentColumns = ["id"],
            childColumns = ["senaId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ProgresoSenaEntity(
    @PrimaryKey val senaId: String,
    val intentos: Int,
    val aciertos: Int,
    val ultimaPracticaEnMillis: Long,
)
