package com.ufps.signaco.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ufps.signaco.core.database.entity.SenaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SenaDao {

    @Query("SELECT * FROM senas ORDER BY orden ASC")
    fun observarSenas(): Flow<List<SenaEntity>>

    @Query("SELECT * FROM senas WHERE id = :senaId")
    fun observarSena(senaId: String): Flow<SenaEntity?>

    @Query("SELECT COUNT(*) FROM senas")
    suspend fun contarSenas(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSenas(senas: List<SenaEntity>)
}
