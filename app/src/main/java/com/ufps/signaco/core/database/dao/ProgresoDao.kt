package com.ufps.signaco.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.ufps.signaco.core.database.entity.ProgresoSenaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Clase abstracta y no interfaz porque [registrarIntento] es un metodo con cuerpo anotado
 * con @Transaction; Room genera la envoltura transaccional de forma confiable en este caso.
 */
@Dao
abstract class ProgresoDao {

    @Query("SELECT * FROM progreso_senas")
    abstract fun observarProgreso(): Flow<List<ProgresoSenaEntity>>

    @Query("SELECT * FROM progreso_senas WHERE senaId = :senaId")
    abstract fun observarProgresoDeSena(senaId: String): Flow<ProgresoSenaEntity?>

    /**
     * Suma un intento (y el acierto si lo hubo) en una sola transaccion, sin leer antes
     * desde Kotlin: asi dos intentos seguidos no se pisan.
     */
    @Transaction
    open suspend fun registrarIntento(senaId: String, acerto: Boolean, momentoEnMillis: Long) {
        crearFilaSiNoExiste(senaId)
        sumarIntento(senaId, if (acerto) 1 else 0, momentoEnMillis)
    }

    @Query(
        """
        INSERT OR IGNORE INTO progreso_senas (senaId, intentos, aciertos, ultimaPracticaEnMillis)
        VALUES (:senaId, 0, 0, 0)
        """
    )
    abstract suspend fun crearFilaSiNoExiste(senaId: String)

    @Query(
        """
        UPDATE progreso_senas
        SET intentos = intentos + 1,
            aciertos = aciertos + :incrementoDeAciertos,
            ultimaPracticaEnMillis = :momentoEnMillis
        WHERE senaId = :senaId
        """
    )
    abstract suspend fun sumarIntento(
        senaId: String,
        incrementoDeAciertos: Int,
        momentoEnMillis: Long,
    )

    /**
     * Borra todo lo que el usuario lleva: intentos, lecciones aprobadas, racha y días activos.
     * En una sola transacción para que no quede un progreso a medias si algo falla.
     */
    @Transaction
    open suspend fun borrarTodo() {
        borrarIntentos()
        borrarLeccionesCompletadas()
        borrarRacha()
        borrarActividad()
    }

    @Query("DELETE FROM progreso_senas")
    abstract suspend fun borrarIntentos()

    @Query("DELETE FROM lecciones_completadas")
    abstract suspend fun borrarLeccionesCompletadas()

    @Query("DELETE FROM racha")
    abstract suspend fun borrarRacha()

    @Query("DELETE FROM actividad_diaria")
    abstract suspend fun borrarActividad()
}
