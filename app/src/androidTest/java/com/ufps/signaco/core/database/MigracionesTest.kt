package com.ufps.signaco.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
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

    private companion object {
        const val BASE = "migracion-test"
    }
}
