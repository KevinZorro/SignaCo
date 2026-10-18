package com.ufps.signaco.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Que la migración no pierda datos de quien ya tenía la app instalada. */
@RunWith(AndroidJUnit4::class)
class MigracionesTest {

    @get:Rule
    val ayudante = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SignaCoDatabase::class.java,
    )

    @Test
    fun deUnoADosConservaLaRachaYMarcaSuUltimoDia() {
        ayudante.createDatabase(BASE, 1).use { v1 ->
            v1.execSQL(
                "INSERT INTO racha (id, diasConsecutivos, mejorRacha, ultimoDiaActivoEnDias) VALUES (0, 3, 5, 100)",
            )
        }

        val v2 = ayudante.runMigrationsAndValidate(BASE, 2, true, MIGRACION_1_2)

        v2.query("SELECT diasConsecutivos FROM racha").use { cursor ->
            cursor.moveToFirst()
            assertEquals(3, cursor.getInt(0))
        }
        v2.query("SELECT dia FROM actividad_diaria").use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            assertEquals(100L, cursor.getLong(0))
        }
    }

    @Test
    fun deUnoADosSinRachaNoInventaDiasActivos() {
        ayudante.createDatabase(BASE, 1).close()

        val v2 = ayudante.runMigrationsAndValidate(BASE, 2, true, MIGRACION_1_2)

        v2.query("SELECT COUNT(*) FROM actividad_diaria").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    fun deDosATresConservaIntentosLeccionesRachaYDiasActivos() {
        ayudante.createDatabase(BASE, 2).use { v2 -> sembrarProgresoV2(v2) }

        val v3 = ayudante.runMigrationsAndValidate(BASE, 3, true, MIGRACION_2_3)

        comprobarProgresoIntacto(v3)
    }

    @Test
    fun deDosATresQuitaLaDescripcionYConservaLasSenas() {
        ayudante.createDatabase(BASE, 2).use { v2 -> sembrarProgresoV2(v2) }

        val v3 = ayudante.runMigrationsAndValidate(BASE, 3, true, MIGRACION_2_3)

        assertFalse("descripcion" in columnasDe(v3, "senas"))
        v3.query("SELECT id, letra, nombre, categoria, orden, disponible FROM senas ORDER BY orden").use { c ->
            assertEquals(2, c.count)
            c.moveToFirst()
            assertEquals("A", c.getString(0))
            assertEquals("Letra A", c.getString(2))
            assertEquals("VOCALES", c.getString(3))
            assertEquals(1, c.getInt(5))
        }
    }

    /**
     * El caso peligroso: con las claves foráneas activas, borrar la tabla vieja de señas borra en
     * cascada los intentos. La migración los respalda y los restaura.
     */
    @Test
    fun deDosATresConClavesForaneasActivasNoPierdeElProgreso() {
        ayudante.createDatabase(BASE, 2).use { v2 ->
            sembrarProgresoV2(v2)
            v2.execSQL("PRAGMA foreign_keys = ON")
            v2.beginTransaction()
            try {
                MIGRACION_2_3.migrate(v2)
                v2.setTransactionSuccessful()
            } finally {
                v2.endTransaction()
            }
            comprobarProgresoIntacto(v2)
        }
    }

    @Test
    fun deUnoATresEnCadenaConservaLaRacha() {
        ayudante.createDatabase(BASE, 1).use { v1 ->
            v1.execSQL(
                "INSERT INTO racha (id, diasConsecutivos, mejorRacha, ultimoDiaActivoEnDias) VALUES (0, 3, 5, 100)",
            )
        }

        val v3 = ayudante.runMigrationsAndValidate(BASE, 3, true, MIGRACION_1_2, MIGRACION_2_3)

        v3.query("SELECT diasConsecutivos, mejorRacha FROM racha").use { c ->
            c.moveToFirst()
            assertEquals(3, c.getInt(0))
            assertEquals(5, c.getInt(1))
        }
        v3.query("SELECT dia FROM actividad_diaria").use { c ->
            c.moveToFirst()
            assertEquals(100L, c.getLong(0))
        }
    }

    /** Un usuario con algo de todo: intentos, una lección aprobada, racha y días activos. */
    private fun sembrarProgresoV2(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO lecciones (id, titulo, descripcion, categoria, orden) " +
                "VALUES ('leccion_vocales', 'Vocales', 'Las vocales', 'VOCALES', 1)",
        )
        db.execSQL(
            "INSERT INTO senas (id, letra, nombre, descripcion, categoria, orden, disponible) VALUES " +
                "('A', 'A', 'Letra A', 'Puño cerrado', 'VOCALES', 1, 1), " +
                "('I', 'I', 'Letra I', 'Meñique arriba', 'VOCALES', 9, 1)",
        )
        db.execSQL(
            "INSERT INTO leccion_senas (leccionId, senaId) VALUES ('leccion_vocales', 'A'), ('leccion_vocales', 'I')",
        )
        db.execSQL(
            "INSERT INTO progreso_senas (senaId, intentos, aciertos, ultimaPracticaEnMillis) VALUES " +
                "('A', 8, 7, 1000), ('I', 3, 1, 2000)",
        )
        db.execSQL(
            "INSERT INTO lecciones_completadas (leccionId, completadaEnMillis, aciertosQuiz, totalQuiz) " +
                "VALUES ('leccion_vocales', 3000, 4, 4)",
        )
        db.execSQL(
            "INSERT INTO racha (id, diasConsecutivos, mejorRacha, ultimoDiaActivoEnDias) VALUES (0, 4, 6, 200)",
        )
        db.execSQL("INSERT INTO actividad_diaria (dia) VALUES (199), (200)")
    }

    private fun comprobarProgresoIntacto(db: SupportSQLiteDatabase) {
        db.query("SELECT senaId, intentos, aciertos, ultimaPracticaEnMillis FROM progreso_senas ORDER BY senaId").use { c ->
            assertEquals(2, c.count)
            c.moveToFirst()
            assertEquals("A", c.getString(0))
            assertEquals(8, c.getInt(1))
            assertEquals(7, c.getInt(2))
            assertEquals(1000L, c.getLong(3))
            c.moveToNext()
            assertEquals("I", c.getString(0))
            assertEquals(3, c.getInt(1))
            assertEquals(1, c.getInt(2))
        }
        db.query("SELECT COUNT(*) FROM leccion_senas").use { c ->
            c.moveToFirst()
            assertEquals(2, c.getInt(0))
        }
        db.query("SELECT aciertosQuiz, totalQuiz FROM lecciones_completadas").use { c ->
            assertEquals(1, c.count)
            c.moveToFirst()
            assertEquals(4, c.getInt(0))
            assertEquals(4, c.getInt(1))
        }
        db.query("SELECT diasConsecutivos, mejorRacha, ultimoDiaActivoEnDias FROM racha").use { c ->
            c.moveToFirst()
            assertEquals(4, c.getInt(0))
            assertEquals(6, c.getInt(1))
            assertEquals(200L, c.getLong(2))
        }
        db.query("SELECT COUNT(*) FROM actividad_diaria").use { c ->
            c.moveToFirst()
            assertEquals(2, c.getInt(0))
        }
        db.query("PRAGMA foreign_key_check").use { c -> assertEquals(0, c.count) }
    }

    private fun columnasDe(db: SupportSQLiteDatabase, tabla: String): List<String> =
        db.query("PRAGMA table_info($tabla)").use { c ->
            buildList { while (c.moveToNext()) add(c.getString(c.getColumnIndexOrThrow("name"))) }
        }

    private companion object {
        const val BASE = "migracion-test"
    }
}
