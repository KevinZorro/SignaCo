package com.ufps.signaco.core.reconocimiento.data

import android.content.Context
import com.ufps.signaco.core.comun.DispatcherDefault
import com.ufps.signaco.core.reconocimiento.domain.ConfiguracionDeReconocimiento
import com.ufps.signaco.core.reconocimiento.domain.ErrorDeModelo
import com.ufps.signaco.core.reconocimiento.domain.NormalizadorDeLandmarks
import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import com.ufps.signaco.core.reconocimiento.domain.SignClassifier
import com.ufps.signaco.core.reconocimiento.domain.VocabularioActivo
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
 * FILTRADO DE VOCABULARIO: el modelo da 28 clases pero la app solo enseña 19 letras. Las
 * probabilidades NO se renormalizan sobre las activas: los umbrales de config_rechazo.json están
 * calibrados contra la distribución cruda de 28 clases, y renormalizar dividiría por una suma
 * menor que 1, inflando las confianzas y aflojando los umbrales en silencio. Se hace argmax
 * restringido a las activas y se compara la probabilidad cruda.
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
    private var indicesActivos: IntArray = IntArray(0)

    override suspend fun clasificar(landmarks: FloatArray): ResultadoClasificacion =
        withContext(dispatcher) {
            if (landmarks.size != NormalizadorDeLandmarks.VALORES) {
                return@withContext ResultadoClasificacion.ManoNoDetectada
            }

            val (interprete, configuracion) = asegurarInicializado()

            val entrada = arrayOf(landmarks)
            val salida = Array(1) { FloatArray(configuracion.totalDeClases) }

            val inicio = System.nanoTime()
            interprete.run(entrada, salida)
            val milisegundos = (System.nanoTime() - inicio) / 1_000_000

            decidir(landmarks, salida[0], configuracion, milisegundos)
        }

    private fun decidir(
        landmarks: FloatArray,
        probabilidades: FloatArray,
        configuracion: ConfiguracionDeReconocimiento,
        milisegundos: Long,
    ): ResultadoClasificacion {
        val ganadorGlobal = indiceMayorEn(probabilidades, probabilidades.indices.toList())

        // El mejor candidato entre las 19 activas, con su probabilidad CRUDA.
        val elegido = indiceMayorEn(probabilidades, indicesActivos.toList())
        val letra = configuracion.letraEn(elegido)
        val confianza = probabilidades.getOrElse(elegido) { 0f }
        val umbral = letra?.let { configuracion.umbralDe(it) } ?: configuracion.umbralGlobal

        // 1. La clase de rechazo manda sobre todo lo demás.
        val ganoElRechazo = ganadorGlobal == configuracion.indiceDeRechazo
        // 2. Modo estricto opcional: si gana una letra excluida, rechazar en vez de caer a la
        //    mejor activa. Ver la nota de MODO_ESTRICTO.
        val ganoUnaExcluida = !ganoElRechazo &&
            configuracion.letraEn(ganadorGlobal)?.let { !VocabularioActivo.estaActiva(it) } == true
        // 3. Umbral crudo de la letra elegida.
        val pasaElUmbral = letra != null && confianza >= umbral

        val resultado = when {
            ganoElRechazo -> ResultadoClasificacion.SenaNoReconocida
            ganoUnaExcluida && MODO_ESTRICTO -> ResultadoClasificacion.SenaNoReconocida
            !pasaElUmbral -> ResultadoClasificacion.SenaNoReconocida
            else -> ResultadoClasificacion.LetraReconocida(letra, confianza)
        }

        registro.registrarFrame(
            landmarks = landmarks,
            probabilidades = probabilidades,
            indiceGanadorGlobal = ganadorGlobal,
            indiceElegido = elegido,
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
        ResultadoClasificacion.SenaNoReconocida -> "SenaNoReconocida"
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
                indicesActivos = VocabularioActivo.indicesActivosEn(it.etiquetas)
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
        const val MODELO = "signaco_abecedario_rechazo.tflite"

        /**
         * Qué hacer cuando el ganador global es una letra EXCLUIDA (no la clase de rechazo).
         *
         * false (permisivo, por defecto): se ignora y se toma la mejor activa. Es lo razonable
         * porque las exclusiones existen precisamente porque las activas se fugan hacia ellas
         * (A, S y T hacia M, N, E en la familia de puño cerrado); rechazar por eso tiraría
         * intentos legítimos. La guarda contra aceptar una seña ajena es el umbral.
         *
         * true (estricto): si gana una excluida, se rechaza. Menos falsos aciertos, más falsos
         * rechazos. Conviene probarlo en dispositivo con el log de depuración, porque algunos
         * umbrales del config son de 0.02 y filtran poco.
         */
        const val MODO_ESTRICTO = false
    }
}
