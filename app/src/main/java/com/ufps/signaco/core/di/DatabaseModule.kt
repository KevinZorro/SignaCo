package com.ufps.signaco.core.di

import android.content.Context
import androidx.room.Room
import com.ufps.signaco.core.database.PrecargaDelCatalogo
import com.ufps.signaco.core.database.MIGRACION_1_2
import com.ufps.signaco.core.database.MIGRACION_2_3
import com.ufps.signaco.core.database.SignaCoDatabase
import com.ufps.signaco.core.database.dao.LeccionDao
import com.ufps.signaco.core.database.dao.ProgresoDao
import com.ufps.signaco.core.database.dao.RachaDao
import com.ufps.signaco.core.database.dao.SenaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun proveerBaseDeDatos(
        @ApplicationContext contexto: Context,
        precarga: PrecargaDelCatalogo,
    ): SignaCoDatabase = Room.databaseBuilder(
        contexto,
        SignaCoDatabase::class.java,
        SignaCoDatabase.NOMBRE,
    ).addCallback(precarga).addMigrations(MIGRACION_1_2, MIGRACION_2_3).build()

    @Provides
    fun proveerSenaDao(base: SignaCoDatabase): SenaDao = base.senaDao()

    @Provides
    fun proveerLeccionDao(base: SignaCoDatabase): LeccionDao = base.leccionDao()

    @Provides
    fun proveerProgresoDao(base: SignaCoDatabase): ProgresoDao = base.progresoDao()

    @Provides
    fun proveerRachaDao(base: SignaCoDatabase): RachaDao = base.rachaDao()
}
