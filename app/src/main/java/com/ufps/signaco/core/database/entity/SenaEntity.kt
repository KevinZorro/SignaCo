package com.ufps.signaco.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Catalogo de senas del abecedario. Se precarga al crear la base y no se modifica en runtime. */
@Entity(tableName = "senas")
data class SenaEntity(
    @PrimaryKey val id: String,
    val letra: String,
    val nombre: String,
    val categoria: String,
    val orden: Int,
    val disponible: Boolean,
)
