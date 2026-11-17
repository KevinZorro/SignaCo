package com.ufps.signaco.core.preferencias.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ufps.signaco.core.preferencias.domain.ManoDominante
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.core.preferencias.domain.PreferenciasUsuario
import com.ufps.signaco.core.preferencias.domain.TamanoDeTexto
import com.ufps.signaco.core.preferencias.domain.VelocidadDeClips
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
                val porDefecto = PreferenciasUsuario()
                PreferenciasUsuario(
                    mostrarPistas = preferencias[CLAVE_PISTAS] ?: porDefecto.mostrarPistas,
                    retroalimentacionHaptica = preferencias[CLAVE_HAPTICA] ?: porDefecto.retroalimentacionHaptica,
                    manoDominante = enumO(preferencias[CLAVE_MANO], porDefecto.manoDominante),
                    velocidadDeClips = enumO(preferencias[CLAVE_VELOCIDAD], porDefecto.velocidadDeClips),
                    subtitulosEnClips = preferencias[CLAVE_SUBTITULOS] ?: porDefecto.subtitulosEnClips,
                    tamanoDeTexto = enumO(preferencias[CLAVE_TAMANO], porDefecto.tamanoDeTexto),
                    altoContraste = preferencias[CLAVE_CONTRASTE] ?: porDefecto.altoContraste,
                    reducirAnimaciones = preferencias[CLAVE_ANIMACIONES] ?: porDefecto.reducirAnimaciones,
                    primerUsoCompletado = preferencias[CLAVE_PRIMER_USO] ?: porDefecto.primerUsoCompletado,
                    recordatoriosActivos = preferencias[CLAVE_RECORDATORIOS] ?: porDefecto.recordatoriosActivos,
                )
            }

    override suspend fun cambiarMostrarPistas(activo: Boolean) {
        contexto.dataStore.edit { it[CLAVE_PISTAS] = activo }
    }

    override suspend fun cambiarRetroalimentacionHaptica(activo: Boolean) {
        contexto.dataStore.edit { it[CLAVE_HAPTICA] = activo }
    }

    override suspend fun cambiarManoDominante(mano: ManoDominante) {
        contexto.dataStore.edit { it[CLAVE_MANO] = mano.name }
    }

    override suspend fun cambiarVelocidadDeClips(velocidad: VelocidadDeClips) {
        contexto.dataStore.edit { it[CLAVE_VELOCIDAD] = velocidad.name }
    }

    override suspend fun cambiarSubtitulosEnClips(activo: Boolean) {
        contexto.dataStore.edit { it[CLAVE_SUBTITULOS] = activo }
    }

    override suspend fun cambiarRecordatoriosActivos(activo: Boolean) {
        contexto.dataStore.edit { it[CLAVE_RECORDATORIOS] = activo }
    }

    override suspend fun guardarApariencia(
        tamano: TamanoDeTexto,
        altoContraste: Boolean,
        reducirAnimaciones: Boolean,
    ) {
        contexto.dataStore.edit {
            it[CLAVE_TAMANO] = tamano.name
            it[CLAVE_CONTRASTE] = altoContraste
            it[CLAVE_ANIMACIONES] = reducirAnimaciones
        }
    }

    override suspend fun marcarPrimerUsoCompletado() {
        contexto.dataStore.edit { it[CLAVE_PRIMER_USO] = true }
    }

    /** Los enum se guardan por nombre. Un nombre desconocido cae al valor por defecto. */
    private inline fun <reified T : Enum<T>> enumO(nombre: String?, porDefecto: T): T =
        enumValues<T>().firstOrNull { it.name == nombre } ?: porDefecto

    private companion object {
        val CLAVE_PISTAS = booleanPreferencesKey("mostrar_pistas")
        val CLAVE_RECORDATORIOS = booleanPreferencesKey("recordatorios_activos")
        val CLAVE_HAPTICA = booleanPreferencesKey("retroalimentacion_haptica")
        val CLAVE_MANO = stringPreferencesKey("mano_dominante")
        val CLAVE_VELOCIDAD = stringPreferencesKey("velocidad_de_clips")
        val CLAVE_SUBTITULOS = booleanPreferencesKey("subtitulos_en_clips")
        val CLAVE_TAMANO = stringPreferencesKey("tamano_de_texto")
        val CLAVE_CONTRASTE = booleanPreferencesKey("alto_contraste")
        val CLAVE_ANIMACIONES = booleanPreferencesKey("reducir_animaciones")
        val CLAVE_PRIMER_USO = booleanPreferencesKey("primer_uso_completado")
    }
}
