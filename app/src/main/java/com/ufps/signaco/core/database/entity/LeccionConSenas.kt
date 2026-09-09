package com.ufps.signaco.core.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/** Proyeccion de una leccion junto con sus senas. */
data class LeccionConSenas(
    @Embedded val leccion: LeccionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = LeccionSenaCrossRef::class,
            parentColumn = "leccionId",
            entityColumn = "senaId",
        ),
    )
    val senas: List<SenaEntity>,
)
