package com.ufps.signaco.core.reconocimiento.data

import android.content.Context
import com.ufps.signaco.core.comun.DispatcherDefault
import com.ufps.signaco.core.reconocimiento.domain.ConfiguracionDeReconocimiento
import com.ufps.signaco.core.reconocimiento.domain.ErrorDeModelo
import com.ufps.signaco.core.reconocimiento.domain.NormalizadorDeLandmarks
import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import com.ufps.signaco.core.reconocimiento.domain.SignClassifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clasificador real. Portado del spike, repartido según las capas del proyecto: aquí queda solo
 * cargar el modelo y decidir la letra de UN frame. El suavizado temporal vive en
 * [com.ufps.signaco.feature.practica.domain.EstabilizadorDePredicciones] y el volcado en
 * [RegistroDeDepuracion].
 *
 * Esta clase es SIN ESTADO entre frames, y eso importa: en el spike el buffer de suavizado vivía
 * dentro del clasificador, que aquí es @Singleton, así que se habría filtrado entre sesiones de
 * práctica y entre pantallas.
 *
 * ENTRADA: los 63 floats YA NORMALIZADOS por [NormalizadorDeLandmarks]. Un arreglo de otro
 * tamaño se interpreta como que no hay mano.
 *
 * REGLA DE DECISIÓN (modelo T-08, contrato v1): ganador = argmax sobre las 28 clases; si es la
 * clase de rechazo se rechaza; si no, se acepta solo si su probabilidad CRUDA alcanza el umbral de
 * esa letra. Es la misma regla con la que se midió el modelo; no se filtra por vocabulario aquí
 * (qué letras se enseñan lo decide el catálogo), así el deletreo puede ver C, E, K, M, N.
 */
@Singleton
class TFLiteSignClassifier @Inject constructor(
    @ApplicationContext private val contexto: Context,
    @DispatcherDefault private val dispatcher: CoroutineDispatcher,
    private val proveedorDeConfiguracion: ProveedorDeConfiguracion,
    private val registro: RegistroDeDepuracion,
) : SignClassifier {

    private val candado = Mutex()

    private var interprete: Interpreter? = null
    private var configuracion: ConfiguracionDeReconocimiento? = null

    override suspend fun clasificar(landmarks: FloatArray): ResultadoClasificacion =
        withContext(dispatcher) {
            if (landmarks.size != NormalizadorDeLandmarks.VALORES) {
                return@withContext ResultadoClasificacion.ManoNoDetectada
            }

            val (interprete, configuracion) = asegurarInicializado()

            val salida = Array(1) { FloatArray(configuracion.totalDeClases) }

            val inicio = System.nanoTime()
            interprete.run(arrayOf(landmarks), salida)
            val milisegundos = (System.nanoTime() - inicio) / 1_000_000

            decidir(landmarks, salida[0], configuracion, milisegundos)
        }

    private fun decidir(
        landmarks: FloatArray,
        probabilidades: FloatArray,
        configuracion: ConfiguracionDeReconocimiento,
        milisegundos: Long,
    ): ResultadoClasificacion {
        val elegido = indiceMayorEn(probabilidades, probabilidades.indices.toList())
        val ganoElRechazo = elegido == configuracion.indiceDeRechazo
        // Si gana el rechazo no hay letra: se informa la mejor letra y su probabilidad cruda solo
        // para la barra de coincidencia en vivo.
        val mejorLetra = indiceMayorEn(probabilidades, configuracion.etiquetas.indices.toList())
        val indiceDeLetra = if (ganoElRechazo) mejorLetra else elegido
        val letra = configuracion.letraEn(indiceDeLetra)
        val confianza = probabilidades.getOrElse(indiceDeLetra) { 0f }
        val umbral = letra?.let { configuracion.umbralDe(it) } ?: configuracion.umbralGlobal
        val pasaElUmbral = confianza >= umbral

        val resultado = when {
            ganoElRechazo || letra == null || !pasaElUmbral ->
                ResultadoClasificacion.SenaNoReconocida(letra, confianza)
            else -> ResultadoClasificacion.LetraReconocida(letra, confianza)
        }

        registro.registrarFrame(
            landmarks = landmarks,
            probabilidades = probabilidades,
            indiceGanadorGlobal = elegido,
            indiceElegido = indiceDeLetra,
            letraElegida = letra,
            confianza = confianza,
            umbral = umbral,
            origenDelUmbral = letra?.let { configuracion.origenDelUmbralDe(it) } ?: "sin letra",
            decision = describir(resultado),
            milisegundosDeInferencia = milisegundos,
            configuracion = configuracion,
            modeloCargado = interprete != null,
        )
        return resultado
    }

    private fun describir(resultado: ResultadoClasificacion): String = when (resultado) {
        is ResultadoClasificacion.LetraReconocida ->
            "LetraReconocida(${resultado.letra}, %.4f)".format(resultado.confianza)
        is ResultadoClasificacion.SenaNoReconocida -> "SenaNoReconocida"
        ResultadoClasificacion.ManoNoDetectada -> "ManoNoDetectada"
    }

    private fun indiceMayorEn(probabilidades: FloatArray, candidatos: List<Int>): Int {
        var mejorIndice = -1
        var mejorValor = -Float.MAX_VALUE
        for (indice in candidatos) {
            val valor = probabilidades.getOrElse(indice) { -Float.MAX_VALUE }
            if (valor > mejorValor) {
                mejorValor = valor
                mejorIndice = indice
            }
        }
        return mejorIndice
    }

    /** El intérprete se crea UNA vez, no por frame. */
    private suspend fun asegurarInicializado(): Pair<Interpreter, ConfiguracionDeReconocimiento> =
        candado.withLock {
            val yaListo = interprete
            val configYaLeida = configuracion
            if (yaListo != null && configYaLeida != null) return@withLock yaListo to configYaLeida

            val config = configYaLeida ?: proveedorDeConfiguracion.obtener().also {
                configuracion = it
            }
            val nuevo = crearInterprete()
            interprete = nuevo

            registro.registrarArranque(
                origenDelModelo = "assets/$MODELO",
                formaDeEntrada = formaDe(nuevo, entrada = true),
                formaDeSalida = formaDe(nuevo, entrada = false),
                origenDeLaConfig = config.procedencia,
                configuracion = config,
            )
            nuevo to config
        }

    private fun crearInterprete(): Interpreter = try {
        contexto.assets.openFd(MODELO).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).use { entrada ->
                // El MappedByteBuffer sigue siendo válido después de cerrar el canal.
                val buffer = entrada.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    descriptor.startOffset,
                    descriptor.declaredLength,
                )
                Interpreter(buffer)
            }
        }
    } catch (e: Exception) {
        throw ErrorDeModelo("No se pudo cargar assets/$MODELO", e)
    }

    private fun formaDe(interprete: Interpreter, entrada: Boolean): String = runCatching {
        val tensor = if (entrada) interprete.getInputTensor(0) else interprete.getOutputTensor(0)
        "shape=${tensor.shape().joinToString("x")} dtype=${tensor.dataType()}"
    }.getOrElse { "no legible: ${it.message}" }

    /** Libera la memoria nativa. Volver a clasificar reinicializa sin problema. */
    fun liberar() {
        interprete?.close()
        interprete = null
    }

    private companion object {
        const val MODELO = "signaco_mlp_t08.tflite"
    }
}
