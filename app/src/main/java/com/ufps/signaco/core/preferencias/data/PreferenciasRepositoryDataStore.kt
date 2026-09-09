package com.ufps.signaco.core.preferencias.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.core.preferencias.domain.PreferenciasUsuario
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val ARCHIVO_DE_PREFERENCIAS = "preferencias_signaco"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = ARCHIVO_DE_PREFERENCIAS,
)

@Singleton
class PreferenciasRepositoryDataStore @Inject constructor(
    @ApplicationContext private val contexto: Context,
) : PreferenciasRepository {

    override fun observarPreferencias(): Flow<PreferenciasUsuario> =
        contexto.dataStore.data
            .catch { error ->
                // Un archivo corrupto no debe tumbar la app: se cae a los valores por defecto.
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferencias ->
                PreferenciasUsuario(
                    mostrarPistas = preferencias[CLAVE_PISTAS] ?: true,
                    retroalimentacionHaptica = preferencias[CLAVE_HAPTICA] ?: true,
                )
            }

    override suspend fun cambiarMostrarPistas(activo: Boolean) {
        contexto.dataStore.edit { it[CLAVE_PISTAS] = activo }
    }

    override suspend fun cambiarRetroalimentacionHaptica(activo: Boolean) {
        contexto.dataStore.edit { it[CLAVE_HAPTICA] = activo }
    }

    private companion object {
        val CLAVE_PISTAS = booleanPreferencesKey("mostrar_pistas")
        val CLAVE_HAPTICA = booleanPreferencesKey("retroalimentacion_haptica")
    }
}
