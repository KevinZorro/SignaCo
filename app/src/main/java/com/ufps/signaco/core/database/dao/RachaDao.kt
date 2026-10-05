package com.ufps.signaco.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.ufps.signaco.core.database.entity.ActividadDiariaEntity
import com.ufps.signaco.core.database.entity.RachaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RachaDao {

    @Query("SELECT * FROM racha WHERE id = :id")
    fun observarRacha(id: Int): Flow<RachaEntity?>

    @Query("SELECT * FROM racha WHERE id = :id")
    suspend fun obtenerRacha(id: Int): RachaEntity?

    @Upsert
    suspend fun guardarRacha(racha: RachaEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registrarDia(actividad: ActividadDiariaEntity)

    @Query("SELECT dia FROM actividad_diaria WHERE dia >= :desde")
    fun observarDiasDesde(desde: Long): Flow<List<Long>>
}
