package com.ufps.signaco.core.di

import com.ufps.signaco.feature.deletreo.data.CargadorDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.ProveedorDeDeletreo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DeletreoModule {

    @Binds
    @Singleton
    abstract fun enlazarProveedorDeDeletreo(implementacion: CargadorDeDeletreo): ProveedorDeDeletreo
}
