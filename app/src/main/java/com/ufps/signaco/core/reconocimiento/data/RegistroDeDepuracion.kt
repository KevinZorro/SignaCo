package com.ufps.signaco.core.reconocimiento.data

import android.util.Log
import com.ufps.signaco.BuildConfig
import com.ufps.signaco.core.reconocimiento.domain.ConfiguracionDeReconocimiento
import com.ufps.signaco.core.reconocimiento.domain.NormalizadorDeLandmarks
import com.ufps.signaco.core.reconocimiento.domain.VocabularioActivo
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Volcado por Logcat para comparar la app contra el notebook de Python. Portado del spike.
 *
 * Está desactivado en release. En debug se puede apagar poniendo [activo] en false, por
 * ejemplo cuando el volcado de 63 floats por frame estorba para medir fps.
 *
 * Filtrar por la etiqueta SignaCO_Debug.
 */
@Singleton
class RegistroDeDepuracion @Inject constructor() {

    var activo: Boolean = BuildConfig.DEBUG

    private var contadorDeFrames = 0L

    private var framesDeCamara = 0
    private var framesConMano = 0
    private var framesSinMano = 0
    private var ultimoLatido = 0L

    /** Volcado único al arrancar: dice si el modelo y la config existen y qué formas espera. */
    fun registrarArranque(
        origenDelModelo: String,
        formaDeEntrada: String,
        formaDeSalida: String,
        origenDeLaConfig: String,
        configuracion: ConfiguracionDeReconocimiento,
    ) {
        if (!activo) return
        Log.i(ETIQUETA, "============== SIGNACO: ARRANQUE DEL CLASIFICADOR ==============")
        Log.i(ETIQUETA, "Modelo               : $origenDelModelo")
        Log.i(ETIQUETA, "Tensor entrada       : $formaDeEntrada")
        Log.i(ETIQUETA, "Tensor salida        : $formaDeSalida")
        Log.i(ETIQUETA, "Config               : $origenDeLaConfig")
        Log.i(ETIQUETA, "Etiquetas (${configuracion.etiquetas.size})     : ${configuracion.etiquetas}")
        Log.i(ETIQUETA, "Total de clases      : ${configuracion.totalDeClases}")
        Log.i(ETIQUETA, "indice_rechazo       : ${configuracion.indiceDeRechazo}")
        Log.i(ETIQUETA, "umbral_global        : ${configuracion.umbralGlobal}")
        Log.i(ETIQUETA, "umbral_por_letra (${configuracion.umbralPorLetra.size}): ${configuracion.umbralPorLetra}")
        Log.i(ETIQUETA, "k (suavizado)        : ${configuracion.k}")
        Log.i(ETIQUETA, "Vocabulario activo (${VocabularioActivo.LETRAS.size}): ${VocabularioActivo.LETRAS}")
        Log.i(ETIQUETA, "Excluidas (${VocabularioActivo.EXCLUIDAS.size})        : ${VocabularioActivo.EXCLUIDAS}")
        Log.i(ETIQUETA, "===============================================================")
    }

    /**
     * LATIDO. Una línea por segundo que demuestra que la cadena está viva, aunque MediaPipe no
     * vea ninguna mano.
     *
     * Sin esto, "no detecta la mano" y "el log no funciona" se veían exactamente igual: silencio.
     * Un frame sin mano no llegaba al clasificador, así que no se registraba nada en absoluto.
     *
     * Se llama desde el analizador en CADA frame que entrega la cámara, así que sigue latiendo
     * aunque MediaPipe deje de invocar su listener.
     */
    fun registrarFrameDeCamara(ancho: Int, alto: Int, rotacion: Int) {
        if (!activo) return
        framesDeCamara++
        val ahora = System.currentTimeMillis()
        if (ultimoLatido == 0L) {
            ultimoLatido = ahora
            return
        }
        val transcurrido = ahora - ultimoLatido
        if (transcurrido < MILISEGUNDOS_ENTRE_LATIDOS) return

        val aviso = when {
            framesConMano == 0 && framesSinMano == 0 ->
                "  <- MediaPipe no está devolviendo NINGÚN resultado: mira el error de arriba"
            framesConMano == 0 ->
                "  <- MediaPipe no ve ninguna mano: acércala, mejora la luz, o revisa la cámara"
            else -> ""
        }
        Log.i(
            ETIQUETA,
            "[LATIDO] %.1f frames/s de cámara | imagen %dx%d rot=%d | MediaPipe: %d con mano, %d sin mano%s"
                .format(
                    framesDeCamara * 1000f / transcurrido, ancho, alto, rotacion,
                    framesConMano, framesSinMano, aviso,
                ),
        )
        ultimoLatido = ahora
        framesDeCamara = 0
        framesConMano = 0
        framesSinMano = 0
    }

    /** Lo llama el analizador con cada resultado de MediaPipe, haya mano o no. */
    fun registrarResultadoDeMediaPipe(huboMano: Boolean) {
        if (!activo) return
        if (huboMano) framesConMano++ else framesSinMano++
    }

    @Suppress("LongParameterList")
    fun registrarFrame(
        landmarks: FloatArray,
        probabilidades: FloatArray,
        indiceGanadorGlobal: Int,
        indiceElegido: Int,
        letraElegida: String?,
        confianza: Float,
        umbral: Float,
        origenDelUmbral: String,
        decision: String,
        milisegundosDeInferencia: Long,
        configuracion: ConfiguracionDeReconocimiento,
        modeloCargado: Boolean,
    ) {
        if (!activo) return
        contadorDeFrames++
        Log.d(ETIQUETA, "==================== FRAME #$contadorDeFrames ====================")

        // 1. El vector de 63 floats que entra al modelo.
        Log.d(ETIQUETA, "[ENTRADA] 63 floats (x0,y0,z0, x1,y1,z1, ...):")
        Log.d(ETIQUETA, "  " + landmarks.joinToString(", ") { "%.5f".format(it) })
        Log.d(ETIQUETA, "  " + NormalizadorDeLandmarks.estadisticasPorEje(landmarks))
        Log.d(
            ETIQUETA,
            "  muñeca normalizada=(%.5f, %.5f, %.5f) (debe ser 0,0,0) | todoCeros=%b"
                .format(landmarks[0], landmarks[1], landmarks[2], landmarks.all { it == 0f }),
        )

        // 2. Las probabilidades completas.
        val suma = probabilidades.sum()
        Log.d(
            ETIQUETA,
            "[SALIDA] ${probabilidades.size} probabilidades (suma=%.6f, un softmax sano da ~1.0):"
                .format(suma),
        )
        Log.d(
            ETIQUETA,
            "  " + probabilidades.mapIndexed { i, p ->
                "$i:${nombreDe(i, configuracion)}=%.4f".format(p)
            }.joinToString("  "),
        )
        val ranking = probabilidades.indices.sortedByDescending { probabilidades[it] }.take(5)
        Log.d(
            ETIQUETA,
            "  top-5: " + ranking.joinToString("  ") {
                "${nombreDe(it, configuracion)}(idx=$it)=%.4f".format(probabilidades[it])
            },
        )

        // 3. Ganador global y ganador tras filtrar el vocabulario.
        Log.d(
            ETIQUETA,
            "[GANADOR GLOBAL] idx=$indiceGanadorGlobal \"${nombreDe(indiceGanadorGlobal, configuracion)}\" " +
                "%.6f | ¿activa? %b".format(
                    probabilidades.getOrElse(indiceGanadorGlobal) { -1f },
                    configuracion.letraEn(indiceGanadorGlobal)
                        ?.let { VocabularioActivo.estaActiva(it) } ?: false,
                ),
        )
        Log.d(
            ETIQUETA,
            "[GANADOR ACTIVO] idx=$indiceElegido \"$letraElegida\" confianza=%.6f (%.2f%%)"
                .format(confianza, confianza * 100f),
        )

        // 4. Umbral aplicado y su procedencia.
        Log.d(ETIQUETA, "[UMBRAL] valor=%.6f  <- %s".format(umbral, origenDelUmbral))
        Log.d(
            ETIQUETA,
            "[UMBRAL] comparación: %.6f %s %.6f"
                .format(confianza, if (confianza >= umbral) ">=" else "<", umbral),
        )
        Log.d(
            ETIQUETA,
            "[RECHAZO] indice_rechazo=${configuracion.indiceDeRechazo} | ¿ganó el rechazo? " +
                "${indiceGanadorGlobal == configuracion.indiceDeRechazo}",
        )
        Log.d(ETIQUETA, "[DECISIÓN] $decision  (inferencia ${milisegundosDeInferencia} ms)")

        // 5. Diagnóstico automático.
        Log.i(
            ETIQUETA,
            "[DIAGNÓSTICO] " + diagnosticar(
                probabilidades, indiceGanadorGlobal, confianza, letraElegida, umbral, suma,
                configuracion, modeloCargado,
            ),
        )
    }

    /**
     * Tiempo de MediaPipe y datos de la normalización, separados de la inferencia.
     * Se imprime justo antes del frame correspondiente, así que en Logcat quedan contiguos.
     */
    fun registrarDeteccion(
        milisegundos: Long,
        handedness: String,
        puntuacion: Float,
        escala: Float,
        espejado: Boolean,
    ) {
        if (!activo) return
        Log.d(
            ETIQUETA,
            "[MEDIAPIPE] detección %d ms | handedness=%s (score=%.3f) | espejadoX=%b | escala(maxNorm)=%.6f"
                .format(milisegundos, handedness, puntuacion, espejado, escala),
        )
    }

    /**
     * Clasifica el fallo en uno de los casos conocidos. Portado del spike: es lo que permite
     * distinguir "la inferencia no corrió" de "la normalización está mal" de "el umbral rechaza".
     */
    @Suppress("LongParameterList", "ReturnCount")
    private fun diagnosticar(
        probabilidades: FloatArray,
        indiceGanadorGlobal: Int,
        confianza: Float,
        letra: String?,
        umbral: Float,
        suma: Float,
        configuracion: ConfiguracionDeReconocimiento,
        modeloCargado: Boolean,
    ): String {
        val uniforme = 1f / probabilidades.size
        return when {
            !modeloCargado ->
                "CASO (0) LA INFERENCIA NO CORRIÓ: el intérprete es nulo, así que la salida quedó " +
                    "en ceros. El .tflite no se pudo cargar. No es un problema de normalización " +
                    "ni de umbral."

            abs(suma) < 1e-4f ->
                ("CASO (0) SIN SALIDA: las %d probabilidades son cero (suma=%.6f) aunque el " +
                    "intérprete existe. Revisar el tamaño del buffer de salida frente al tensor.")
                    .format(probabilidades.size, suma)

            abs(suma - 1f) > 0.05f ->
                ("ANÓMALO: la salida no suma 1 (suma=%.6f). O el modelo no termina en softmax, " +
                    "y entonces los umbrales del config no son probabilidades, o el tamaño del " +
                    "buffer (%d) no coincide con el del modelo.")
                    .format(suma, probabilidades.size)

            confianza < uniforme * 1.5f ->
                ("CASO (a) DISTRIBUCIÓN CASI UNIFORME: la máxima es %.4f frente a %.4f de un " +
                    "reparto plano. El modelo recibe basura -> revisar la normalización.")
                    .format(confianza, uniforme)

            indiceGanadorGlobal == configuracion.indiceDeRechazo ->
                ("CASO (b) GANA no_es_seña CON %.4f. El vector tiene forma válida pero cae fuera " +
                    "de la distribución de entrenamiento -> normalización mal de otra forma, o el " +
                    "espejado aplicado al revés. Ver el espejadoX de la línea [MEDIAPIPE] de arriba.")
                    .format(probabilidades[indiceGanadorGlobal])

            configuracion.letraEn(indiceGanadorGlobal)
                ?.let { !VocabularioActivo.estaActiva(it) } == true ->
                ("CASO (d) GANA UNA LETRA EXCLUIDA: \"%s\" con %.4f. Se ignoró y se tomó la mejor " +
                    "activa (\"%s\", %.4f). Es el filtrado de vocabulario haciendo su trabajo; si " +
                    "pasa demasiado, revisar si conviene el modo estricto.")
                    .format(
                        configuracion.letraEn(indiceGanadorGlobal),
                        probabilidades[indiceGanadorGlobal], letra, confianza,
                    )

            confianza < umbral ->
                ("CASO (c) LA LETRA \"%s\" GANA CON %.4f PERO EL UMBRAL %.4f LA RECHAZA. El " +
                    "problema está en la lectura del config, no en la normalización.")
                    .format(letra, confianza, umbral)

            else -> "OK: \"%s\" reconocida con %.4f (umbral %.4f).".format(letra, confianza, umbral)
        }
    }

    private fun nombreDe(indice: Int, configuracion: ConfiguracionDeReconocimiento): String = when {
        indice == configuracion.indiceDeRechazo -> "no_es_seña"
        else -> configuracion.letraEn(indice) ?: "?$indice"
    }

    private companion object {
        const val ETIQUETA = "SignaCO_Debug"
        const val MILISEGUNDOS_ENTRE_LATIDOS = 1000L
    }
}
