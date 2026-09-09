package com.ufps.signaco.core.di

import com.ufps.signaco.core.comun.Reloj
import com.ufps.signaco.core.comun.RelojDelSistema
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RelojModule {

    @Binds
    @Singleton
    abstract fun enlazarReloj(implementacion: RelojDelSistema): Reloj
}
