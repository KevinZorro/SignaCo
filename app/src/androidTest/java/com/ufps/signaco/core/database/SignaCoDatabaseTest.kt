package com.ufps.signaco.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ufps.signaco.core.database.entity.LeccionCompletadaEntity
import com.ufps.signaco.core.database.entity.RachaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Provider

/**
 * Pruebas de la capa de persistencia contra un SQLite de verdad, en memoria.
 *
 * Verifican lo que el compilador no puede: que la precarga corre, que la relación entre
 * lecciones y señas devuelve lo esperado y que el registro de intentos acumula bien.
 */
@RunWith(AndroidJUnit4::class)
class SignaCoDatabaseTest {

    private lateinit var base: SignaCoDatabase
    private lateinit var alcance: CoroutineScope

    @Before
    fun crearBase() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        alcance = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val proveedor = object : Provider<SignaCoDatabase> {
            override fun get(): SignaCoDatabase = base
        }
        base = Room.inMemoryDatabaseBuilder(contexto, SignaCoDatabase::class.java)
            .addCallback(PrecargaDelCatalogo(proveedor, alcance))
            .build()
    }

    @After
    fun cerrarBase() {
        base.close()
        alcance.cancel()
    }

    /**
     * La precarga se lanza fuera de la transacción de creación, así que es asíncrona:
     * hay que esperarla en vez de asumir que ya ocurrió.
     */
    private suspend fun esperarCatalogo() {
        withTimeout(TIEMPO_MAXIMO_MS) {
            while (base.senaDao().contarSenas() < CatalogoInicial.SENAS.size) {
                delay(25)
            }
            while (base.leccionDao().contarLecciones() < CatalogoInicial.LECCIONES.size) {
                delay(25)
            }
        }
    }

    @Test
    fun laBaseSeCreaYPrecargaElCatalogo() = runBlocking {
        esperarCatalogo()

        assertEquals(27, base.senaDao().contarSenas())
        assertEquals(5, base.leccionDao().contarLecciones())
    }

    @Test
    fun lasSenasSeObservanEnOrdenAlfabetico() = runBlocking {
        esperarCatalogo()

        val senas = base.senaDao().observarSenas().first()

        assertEquals(CatalogoInicial.SENAS.map { it.id }, senas.map { it.id })
    }

    @Test
    fun unaLeccionDevuelveSusSenas() = runBlocking {
        esperarCatalogo()

        val leccion = base.leccionDao()
            .observarLeccionConSenas(CatalogoInicial.LECCION_VOCALES)
            .first()

        assertNotNull(leccion)
        assertEquals(
            listOf("A", "E", "I", "O", "U"),
            leccion!!.senas.sortedBy { it.orden }.map { it.id },
        )
    }

    @Test
    fun unaLeccionQueNoExisteDevuelveNulo() = runBlocking {
        esperarCatalogo()

        assertNull(base.leccionDao().observarLeccionConSenas("no_existe").first())
    }

    @Test
    fun registrarIntentosAcumulaSobreLaMismaSena() = runBlocking {
        esperarCatalogo()
        val progresoDao = base.progresoDao()

        progresoDao.registrarIntento("A", acerto = true, momentoEnMillis = 1_000)
        progresoDao.registrarIntento("A", acerto = false, momentoEnMillis = 2_000)
        progresoDao.registrarIntento("A", acerto = true, momentoEnMillis = 3_000)

        val progreso = progresoDao.observarProgresoDeSena("A").first()
        assertNotNull(progreso)
        assertEquals(3, progreso!!.intentos)
        assertEquals(2, progreso.aciertos)
        assertEquals(3_000L, progreso.ultimaPracticaEnMillis)
    }

    @Test
    fun cadaSenaLlevaSuPropioConteo() = runBlocking {
        esperarCatalogo()
        val progresoDao = base.progresoDao()

        progresoDao.registrarIntento("A", acerto = true, momentoEnMillis = 1_000)
        progresoDao.registrarIntento("B", acerto = false, momentoEnMillis = 1_000)

        val progreso = progresoDao.observarProgreso().first().associateBy { it.senaId }
        assertEquals(2, progreso.size)
        assertEquals(1, progreso.getValue("A").aciertos)
        assertEquals(0, progreso.getValue("B").aciertos)
    }

    @Test
    fun unaLeccionCompletadaNoSeDuplica() = runBlocking {
        esperarCatalogo()
        val leccionDao = base.leccionDao()

        leccionDao.guardarLeccionCompletada(
            LeccionCompletadaEntity(CatalogoInicial.LECCION_VOCALES, 1_000, 4, 5),
        )
        leccionDao.guardarLeccionCompletada(
            LeccionCompletadaEntity(CatalogoInicial.LECCION_VOCALES, 2_000, 5, 5),
        )

        val completadas = leccionDao.observarLeccionesCompletadas().first()
        assertEquals(1, completadas.size)
        assertEquals(5, completadas.single().aciertosQuiz)
    }

    @Test
    fun laRachaEsUnaSolaFila() = runBlocking {
        esperarCatalogo()
        val rachaDao = base.rachaDao()

        rachaDao.guardarRacha(RachaEntity(diasConsecutivos = 1, mejorRacha = 1, ultimoDiaActivoEnDias = 10))
        rachaDao.guardarRacha(RachaEntity(diasConsecutivos = 2, mejorRacha = 2, ultimoDiaActivoEnDias = 11))

        val racha = rachaDao.obtenerRacha(RachaEntity.FILA_UNICA)
        assertNotNull(racha)
        assertEquals(2, racha!!.diasConsecutivos)
        assertEquals(11L, racha.ultimoDiaActivoEnDias)
    }

    @Test
    fun sinActividadNoHayFilaDeRacha() = runBlocking {
        esperarCatalogo()

        assertNull(base.rachaDao().obtenerRacha(RachaEntity.FILA_UNICA))
    }

    @Test
    fun volverASembrarNoDuplicaElCatalogo() = runBlocking {
        esperarCatalogo()

        // La precarga se puede disparar más de una vez (onCreate y onOpen). Como las claves
        // primarias son estables, insertar de nuevo reemplaza en vez de duplicar.
        base.senaDao().insertarSenas(CatalogoInicial.SENAS)
        base.leccionDao().insertarLecciones(CatalogoInicial.LECCIONES)
        base.leccionDao().insertarRelaciones(CatalogoInicial.RELACIONES)

        assertEquals(27, base.senaDao().contarSenas())
        assertEquals(5, base.leccionDao().contarLecciones())
        assertEquals(
            listOf("A", "E", "I", "O", "U"),
            base.leccionDao()
                .observarLeccionConSenas(CatalogoInicial.LECCION_VOCALES)
                .first()!!
                .senas
                .sortedBy { it.orden }
                .map { it.id },
        )
    }

    private companion object {
        const val TIEMPO_MAXIMO_MS = 10_000L
    }
}
