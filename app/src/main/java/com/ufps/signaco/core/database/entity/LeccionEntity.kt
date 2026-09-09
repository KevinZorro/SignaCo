package com.ufps.signaco.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Leccion del catalogo. Tambien se precarga al crear la base. */
@Entity(tableName = "lecciones")
data class LeccionEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val orden: Int,
)
