package com.ufps.signaco.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
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
}
