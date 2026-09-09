package com.ufps.signaco.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/** Una fila por leccion aprobada, con el resultado del quiz que la cerro. */
@Entity(
    tableName = "lecciones_completadas",
    foreignKeys = [
        ForeignKey(
            entity = LeccionEntity::class,
            parentColumns = ["id"],
            childColumns = ["leccionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class LeccionCompletadaEntity(
    @PrimaryKey val leccionId: String,
    val completadaEnMillis: Long,
    val aciertosQuiz: Int,
    val totalQuiz: Int,
)
