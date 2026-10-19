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

/**
 * v2 -> v3: la tabla `senas` pierde la columna `descripcion` (las descripciones de texto se
 * quitaron porque nunca se validaron; la seña se enseña con su imagen de referencia).
 *
 * SQLite no tiene DROP COLUMN antes de Android 14, así que la tabla se recrea: se crea la nueva
 * sin la columna, se copian las filas, se borra la vieja y la nueva toma su nombre.
 *
 * El cuidado está en el progreso. `progreso_senas` y `leccion_senas` apuntan a `senas` con
 * ON DELETE CASCADE: si las claves foráneas estuvieran activas, borrar la tabla vieja borraría
 * en cascada los intentos del usuario. Para no depender de eso, ambas tablas se respaldan antes
 * en tablas temporales y se restauran al final. Lecciones aprobadas, racha y días activos no
 * dependen de `senas` y no se tocan.
 */
val MIGRACION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Respaldo de lo que cuelga de `senas`.
        db.execSQL("CREATE TEMP TABLE respaldo_progreso AS SELECT * FROM progreso_senas")
        db.execSQL("CREATE TEMP TABLE respaldo_leccion_senas AS SELECT * FROM leccion_senas")

        // 2. Tabla nueva sin `descripcion`, con las mismas filas.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `senas_nueva` (`id` TEXT NOT NULL, `letra` TEXT NOT NULL,
            `nombre` TEXT NOT NULL, `categoria` TEXT NOT NULL, `orden` INTEGER NOT NULL,
            `disponible` INTEGER NOT NULL, PRIMARY KEY(`id`))
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO senas_nueva (id, letra, nombre, categoria, orden, disponible)
            SELECT id, letra, nombre, categoria, orden, disponible FROM senas
            """.trimIndent(),
        )

        // 3. La nueva reemplaza a la vieja. Las otras tablas apuntan a `senas` por nombre, así
        //    que quedan apuntando a la nueva.
        db.execSQL("DROP TABLE senas")
        db.execSQL("ALTER TABLE senas_nueva RENAME TO senas")

        // 4. Se restaura lo que la cascada hubiera podido borrar. OR IGNORE: si no se borró nada,
        //    las filas ya están y no se duplican.
        db.execSQL("INSERT OR IGNORE INTO progreso_senas SELECT * FROM respaldo_progreso")
        db.execSQL("INSERT OR IGNORE INTO leccion_senas SELECT * FROM respaldo_leccion_senas")
        db.execSQL("DROP TABLE respaldo_progreso")
        db.execSQL("DROP TABLE respaldo_leccion_senas")

        // 5. Ninguna fila puede quedar apuntando a una seña que no existe.
        db.query("PRAGMA foreign_key_check").use { cursor ->
            check(cursor.count == 0) { "La migración 2 a 3 dejó referencias rotas" }
        }
    }
}
