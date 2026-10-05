package com.ufps.signaco.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: tabla de días con actividad para la racha semanal. El último día activo que ya
 * estaba guardado en la racha se copia, para que la semana en curso no aparezca vacía.
 */
val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `actividad_diaria` (`dia` INTEGER NOT NULL, PRIMARY KEY(`dia`))",
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO actividad_diaria (dia)
            SELECT ultimoDiaActivoEnDias FROM racha WHERE ultimoDiaActivoEnDias > ${Long.MIN_VALUE}
            """.trimIndent(),
        )
    }
}
