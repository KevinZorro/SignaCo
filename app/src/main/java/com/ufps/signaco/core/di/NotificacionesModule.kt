package com.ufps.signaco.core.di

import com.ufps.signaco.core.notificaciones.data.ProgramadorDeRecordatoriosAlarmManager
import com.ufps.signaco.core.notificaciones.domain.ProgramadorDeRecordatorios
import com.ufps.signaco.core.notificaciones.domain.ProveedorDeMensajesRecordatorio
import com.ufps.signaco.core.notificaciones.domain.ProveedorDeMensajesRecordatorioAleatorio
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificacionesModule {

    @Binds
    @Singleton
    abstract fun enlazarProgramadorDeRecordatorios(
        implementacion: ProgramadorDeRecordatoriosAlarmManager,
    ): ProgramadorDeRecordatorios

    companion object {
        @Provides
        @Singleton
        fun proveerProveedorDeMensajes(): ProveedorDeMensajesRecordatorio =
            ProveedorDeMensajesRecordatorioAleatorio()
    }
}
