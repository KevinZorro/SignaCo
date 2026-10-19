package com.ufps.signaco.core.di

import com.ufps.signaco.core.preferencias.data.PreferenciasRepositoryDataStore
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.core.senas.data.ReferenciasEnRecursos
import com.ufps.signaco.core.senas.data.SenaRepositoryRoom
import com.ufps.signaco.core.senas.domain.ReferenciasVisuales
import com.ufps.signaco.core.senas.domain.SenaRepository
import com.ufps.signaco.feature.lecciones.data.LeccionRepositoryRoom
import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import com.ufps.signaco.feature.progreso.data.ProgresoRepositoryRoom
import com.ufps.signaco.feature.progreso.data.RachaRepositoryRoom
import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import com.ufps.signaco.feature.progreso.domain.RachaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Enlaza cada interfaz de dominio con su implementación de datos. Es el único punto donde
 * el dominio se entera de que existe Room.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun enlazarSenaRepository(implementacion: SenaRepositoryRoom): SenaRepository

    /** Hoy, imágenes en res/drawable-nodpi. Para usar fotos o video basta cambiar esta línea. */
    @Binds
    @Singleton
    abstract fun enlazarReferenciasVisuales(
        implementacion: ReferenciasEnRecursos,
    ): ReferenciasVisuales

    @Binds
    @Singleton
    abstract fun enlazarLeccionRepository(implementacion: LeccionRepositoryRoom): LeccionRepository

    @Binds
    @Singleton
    abstract fun enlazarProgresoRepository(
        implementacion: ProgresoRepositoryRoom,
    ): ProgresoRepository

    @Binds
    @Singleton
    abstract fun enlazarRachaRepository(implementacion: RachaRepositoryRoom): RachaRepository

    @Binds
    @Singleton
    abstract fun enlazarPreferenciasRepository(
        implementacion: PreferenciasRepositoryDataStore,
    ): PreferenciasRepository
}
