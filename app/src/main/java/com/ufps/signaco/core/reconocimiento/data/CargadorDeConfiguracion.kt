package com.ufps.signaco.core.reconocimiento.data

import android.content.Context
import com.ufps.signaco.core.comun.DispatcherIo
import com.ufps.signaco.core.reconocimiento.domain.ConfiguracionDeReconocimiento
import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lee config_rechazo.json de assets.
 *
 * El spike usaba Gson; aquí se usa kotlinx.serialization, que ya está en el proyecto por las
 * rutas de navegación. Es una dependencia menos, no un cambio de comportamiento.
 */
@Singleton
class CargadorDeConfiguracion @Inject constructor(
    @ApplicationContext private val contexto: Context,
    @DispatcherIo private val dispatcher: CoroutineDispatcher,
) : ProveedorDeConfiguracion {

    private val candado = Mutex()
    private var enMemoria: ConfiguracionDeReconocimiento? = null

    /** El JSON se lee del disco una sola vez. */
    override suspend fun obtener(): ConfiguracionDeReconocimiento = candado.withLock {
        enMemoria ?: leer().also { enMemoria = it }
    }

    private suspend fun leer(): ConfiguracionDeReconocimiento = withContext(dispatcher) {
        try {
            val texto = contexto.assets.open(ARCHIVO).bufferedReader().use { it.readText() }
            val json = Json { ignoreUnknownKeys = true }.decodeFromString<ConfigJson>(texto)

            val etiquetas = json.etiquetas
            check(!etiquetas.isNullOrEmpty()) { "'etiquetas' vacío o ausente en $ARCHIVO" }

            ConfiguracionDeReconocimiento(
                etiquetas = etiquetas,
                indiceDeRechazo = json.indiceDeRechazo ?: etiquetas.size,
                umbralPorLetra = json.umbralPorLetra.orEmpty(),
                umbralGlobal = json.umbralGlobal
                    ?: ConfiguracionDeReconocimiento.UMBRAL_POR_DEFECTO,
                k = json.k ?: ConfiguracionDeReconocimiento.K_POR_DEFECTO,
                procedencia = "assets/$ARCHIVO",
            )
        } catch (e: Exception) {
            ConfiguracionDeReconocimiento.RESPALDO.copy(
                procedencia = "RESPALDO EN CÓDIGO (la lectura de $ARCHIVO falló: ${e.message})",
            )
        }
    }

    @Serializable
    private data class ConfigJson(
        val modo: String? = null,
        val k: Int? = null,
        val etiquetas: List<String>? = null,
        @SerialName("indice_rechazo") val indiceDeRechazo: Int? = null,
        @SerialName("umbral_por_letra") val umbralPorLetra: Map<String, Float>? = null,
        @SerialName("umbral_global_fallback") val umbralGlobal: Float? = null,
    )

    private companion object {
        const val ARCHIVO = "config_rechazo.json"
    }
}
