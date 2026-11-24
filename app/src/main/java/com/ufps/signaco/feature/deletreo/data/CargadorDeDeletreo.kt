package com.ufps.signaco.feature.deletreo.data

import android.content.Context
import com.ufps.signaco.core.comun.DispatcherIo
import com.ufps.signaco.feature.deletreo.domain.ConfiguracionDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.ConfiguracionDeGestos
import com.ufps.signaco.feature.deletreo.domain.DatosDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.Palabras
import com.ufps.signaco.feature.deletreo.domain.ProveedorDeDeletreo
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

/** Lee config_deletreo.json y palabras_deletreo.txt de assets, una sola vez. */
@Singleton
class CargadorDeDeletreo @Inject constructor(
    @ApplicationContext private val contexto: Context,
    @DispatcherIo private val dispatcher: CoroutineDispatcher,
) : ProveedorDeDeletreo {

    private val candado = Mutex()
    private var enMemoria: DatosDeDeletreo? = null

    override suspend fun obtener(): DatosDeDeletreo = candado.withLock {
        enMemoria ?: leer().also { enMemoria = it }
    }

    private suspend fun leer(): DatosDeDeletreo = withContext(dispatcher) {
        val texto = contexto.assets.open(CONFIG).bufferedReader().use { it.readText() }
        val json = Json { ignoreUnknownKeys = true }.decodeFromString<ConfigJson>(texto)
        val base = ConfiguracionDeDeletreo()
        val config = ConfiguracionDeDeletreo(
            sostenerMs = json.sostenerMs ?: base.sostenerMs,
            minimoDeFrames = json.minimoDeFrames ?: base.minimoDeFrames,
            soltarMs = json.soltarMs ?: base.soltarMs,
            guardaDeEntradaMs = json.guardaDeEntradaMs ?: base.guardaDeEntradaMs,
            pausaDePalabraMs = json.pausaDePalabraMs ?: base.pausaDePalabraMs,
            maximoDeLetras = json.maximoDeLetras ?: base.maximoDeLetras,
            letrasExcluidas = json.letrasExcluidas?.toSet() ?: base.letrasExcluidas,
            costoMinimoDeSustitucion = json.costoMinimoDeSustitucion ?: base.costoMinimoDeSustitucion,
            costoDeBorrarEnElBorde = json.costoDeBorrarEnElBorde ?: base.costoDeBorrarEnElBorde,
            costoMaximoPorLongitud = json.costoMaximoPorLongitud ?: base.costoMaximoPorLongitud,
            margenDeAmbiguedad = json.margenDeAmbiguedad ?: base.margenDeAmbiguedad,
            maximoDeCandidatas = json.maximoDeCandidatas ?: base.maximoDeCandidatas,
            gestos = json.gestos?.let {
                ConfiguracionDeGestos(
                    extendidoMin = it.extendidoMin ?: base.gestos.extendidoMin,
                    dobladoMax = it.dobladoMax ?: base.gestos.dobladoMax,
                    aperturaMin = it.aperturaMin ?: base.gestos.aperturaMin,
                    pulgarAfueraMin = it.pulgarAfueraMin ?: base.gestos.pulgarAfueraMin,
                )
            } ?: base.gestos,
        )
        val lineas = contexto.assets.open(PALABRAS).bufferedReader().use { it.readLines() }
        DatosDeDeletreo(config, Palabras.cargar(lineas, config.letrasExcluidas))
    }

    @Serializable
    private data class ConfigJson(
        @SerialName("sostener_ms") val sostenerMs: Long? = null,
        @SerialName("minimo_de_frames") val minimoDeFrames: Int? = null,
        @SerialName("soltar_ms") val soltarMs: Long? = null,
        @SerialName("guarda_de_entrada_ms") val guardaDeEntradaMs: Long? = null,
        @SerialName("pausa_de_palabra_ms") val pausaDePalabraMs: Long? = null,
        @SerialName("maximo_de_letras") val maximoDeLetras: Int? = null,
        @SerialName("letras_excluidas") val letrasExcluidas: List<String>? = null,
        @SerialName("costo_minimo_de_sustitucion") val costoMinimoDeSustitucion: Float? = null,
        @SerialName("costo_de_borrar_en_el_borde") val costoDeBorrarEnElBorde: Float? = null,
        @SerialName("costo_maximo_por_longitud") val costoMaximoPorLongitud: Map<String, Float>? = null,
        @SerialName("margen_de_ambiguedad") val margenDeAmbiguedad: Float? = null,
        @SerialName("maximo_de_candidatas") val maximoDeCandidatas: Int? = null,
        @SerialName("gestos") val gestos: GestosJson? = null,
    )

    @Serializable
    private data class GestosJson(
        @SerialName("extendido_min") val extendidoMin: Float? = null,
        @SerialName("doblado_max") val dobladoMax: Float? = null,
        @SerialName("apertura_min") val aperturaMin: Float? = null,
        @SerialName("pulgar_afuera_min") val pulgarAfueraMin: Float? = null,
    )

    private companion object {
        const val CONFIG = "config_deletreo.json"
        const val PALABRAS = "palabras_deletreo.txt"
    }
}
