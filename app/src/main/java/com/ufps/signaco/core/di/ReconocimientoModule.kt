package com.ufps.signaco.core.di

import com.ufps.signaco.core.reconocimiento.data.FakeSignClassifier
import com.ufps.signaco.core.reconocimiento.domain.SignClassifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RNF-21: el modelo es reemplazable sin tocar presentation ni domain.
 *
 * Para pasar al modelo real, cambiar FakeSignClassifier por TFLiteSignClassifier en la linea
 * de abajo. Es el unico cambio necesario en toda la app.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ReconocimientoModule {

    @Binds
    @Singleton
    abstract fun enlazarClasificador(implementacion: FakeSignClassifier): SignClassifier
}
