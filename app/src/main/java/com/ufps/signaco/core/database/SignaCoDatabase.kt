package com.ufps.signaco.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ufps.signaco.core.database.dao.LeccionDao
import com.ufps.signaco.core.database.dao.ProgresoDao
import com.ufps.signaco.core.database.dao.RachaDao
import com.ufps.signaco.core.database.dao.SenaDao
import com.ufps.signaco.core.database.entity.ActividadDiariaEntity
import com.ufps.signaco.core.database.entity.LeccionCompletadaEntity
import com.ufps.signaco.core.database.entity.LeccionEntity
import com.ufps.signaco.core.database.entity.LeccionSenaCrossRef
import com.ufps.signaco.core.database.entity.ProgresoSenaEntity
import com.ufps.signaco.core.database.entity.RachaEntity
import com.ufps.signaco.core.database.entity.SenaEntity

/**
 * Base local de la app. Toda la informacion vive aqui, en el dispositivo (RNF-06 a RNF-08).
 *
 * Al subir la version hay que escribir la migracion correspondiente. Los esquemas exportados
 * quedan en app/schemas para poder revisarlas.
 */
@Database(
    entities = [
        SenaEntity::class,
        LeccionEntity::class,
        LeccionSenaCrossRef::class,
        ProgresoSenaEntity::class,
        LeccionCompletadaEntity::class,
        RachaEntity::class,
        ActividadDiariaEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class SignaCoDatabase : RoomDatabase() {

    abstract fun senaDao(): SenaDao

    abstract fun leccionDao(): LeccionDao

    abstract fun progresoDao(): ProgresoDao

    abstract fun rachaDao(): RachaDao

    companion object {
        const val NOMBRE = "signaco.db"
    }
}
