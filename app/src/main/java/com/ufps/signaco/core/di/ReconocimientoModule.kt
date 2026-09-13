package com.ufps.signaco.core.di

import com.ufps.signaco.core.reconocimiento.data.CargadorDeConfiguracion
import com.ufps.signaco.core.reconocimiento.data.TFLiteSignClassifier
import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import com.ufps.signaco.core.reconocimiento.domain.SignClassifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RNF-21: el modelo es reemplazable sin tocar presentation ni domain.
 *
 * La linea de abajo es el unico sitio donde se elige la implementacion. Para volver al
 * clasificador de mentira y desarrollar sin dispositivo, cambiar TFLiteSignClassifier por
 * FakeSignClassifier y no hace falta tocar nada mas.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ReconocimientoModule {

    @Binds
    @Singleton
    abstract fun enlazarClasificador(implementacion: TFLiteSignClassifier): SignClassifier

    @Binds
    @Singleton
    abstract fun enlazarProveedorDeConfiguracion(
        implementacion: CargadorDeConfiguracion,
    ): ProveedorDeConfiguracion
}
