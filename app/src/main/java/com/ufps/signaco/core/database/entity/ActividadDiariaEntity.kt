package com.ufps.signaco.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Un día con actividad, en días desde 1970-01-01. Alimenta los días de la semana de la racha. */
@Entity(tableName = "actividad_diaria")
data class ActividadDiariaEntity(
    @PrimaryKey val dia: Long,
)
