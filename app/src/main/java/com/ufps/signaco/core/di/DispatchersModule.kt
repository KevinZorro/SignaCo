package com.ufps.signaco.core.di

import com.ufps.signaco.core.comun.AlcanceDeAplicacion
import com.ufps.signaco.core.comun.DispatcherDefault
import com.ufps.signaco.core.comun.DispatcherIo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @DispatcherIo
    fun proveerDispatcherIo(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DispatcherDefault
    fun proveerDispatcherDefault(): CoroutineDispatcher = Dispatchers.Default

    /** Alcance que vive tanto como el proceso. Solo para trabajo que no pertenece a una pantalla. */
    @Provides
    @Singleton
    @AlcanceDeAplicacion
    fun proveerAlcanceDeAplicacion(
        @DispatcherIo dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
