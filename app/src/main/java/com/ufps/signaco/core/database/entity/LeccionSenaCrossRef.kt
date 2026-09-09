package com.ufps.signaco.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Que senas contiene cada leccion. Relacion muchos a muchos. */
@Entity(
    tableName = "leccion_senas",
    primaryKeys = ["leccionId", "senaId"],
    foreignKeys = [
        ForeignKey(
            entity = LeccionEntity::class,
            parentColumns = ["id"],
            childColumns = ["leccionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SenaEntity::class,
            parentColumns = ["id"],
            childColumns = ["senaId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("senaId")],
)
data class LeccionSenaCrossRef(
    val leccionId: String,
    val senaId: String,
)
