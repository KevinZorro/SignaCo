package com.ufps.signaco.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.ufps.signaco.core.database.entity.LeccionCompletadaEntity
import com.ufps.signaco.core.database.entity.LeccionConSenas
import com.ufps.signaco.core.database.entity.LeccionEntity
import com.ufps.signaco.core.database.entity.LeccionSenaCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface LeccionDao {

    @Transaction
    @Query("SELECT * FROM lecciones ORDER BY orden ASC")
    fun observarLeccionesConSenas(): Flow<List<LeccionConSenas>>

    @Transaction
    @Query("SELECT * FROM lecciones WHERE id = :leccionId")
    fun observarLeccionConSenas(leccionId: String): Flow<LeccionConSenas?>

    @Query("SELECT * FROM lecciones_completadas")
    fun observarLeccionesCompletadas(): Flow<List<LeccionCompletadaEntity>>

    @Query("SELECT COUNT(*) FROM lecciones")
    suspend fun contarLecciones(): Int

    @Upsert
    suspend fun guardarLeccionCompletada(leccion: LeccionCompletadaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLecciones(lecciones: List<LeccionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRelaciones(relaciones: List<LeccionSenaCrossRef>)
}
