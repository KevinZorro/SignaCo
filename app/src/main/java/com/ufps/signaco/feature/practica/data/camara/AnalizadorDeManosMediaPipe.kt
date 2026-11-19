package com.ufps.signaco.feature.practica.data.camara

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.ufps.signaco.core.reconocimiento.data.RegistroDeDepuracion
import com.ufps.signaco.core.reconocimiento.domain.NormalizadorDeLandmarks
import com.ufps.signaco.feature.practica.domain.model.FrameDeLandmarks

/**
 * Extrae landmarks de mano con MediaPipe en modo LIVE_STREAM y los deja normalizados.
 *
 * Es el único sitio de la app que conoce a MediaPipe. Lo que sale de aquí es un
 * [FrameDeLandmarks] del dominio: 63 floats. No se guarda ni una imagen (RNF-06).
 *
 * SOBRE EL ESPEJADO, que es donde se rompe todo en silencio:
 * MediaPipe decide el `handedness` asumiendo que la imagen viene espejada, como la ve uno en un
 * selfie. Por eso, con la cámara frontal, el bitmap se voltea antes de analizarlo. Ese volteo y
 * el que hace [NormalizadorDeLandmarks] cuando la mano es izquierda son cosas distintas y
 * ninguna sustituye a la otra.
 *
 * Si las letras salen cambiadas de forma sistemática, los dos sospechosos son estos. El log de
 * depuración imprime `handedness` y `espejadoX` en cada frame justo para poder distinguirlos.
 */
class AnalizadorDeManosMediaPipe(
    contexto: Context,
    private val registro: RegistroDeDepuracion,
    private val alDetectar: (FrameDeLandmarks) -> Unit,
) {

    private var inicioDeDeteccion: Long = 0

    /** Del último bitmap enviado a MediaPipe. Lo lee el callback, que llega en otro hilo. */
    @Volatile
    private var proporcionDeImagen: Float = 0f

    private val detector: HandLandmarker = try {
        val opcionesBase = BaseOptions.builder()
            .setModelAssetPath(MODELO)
            .setDelegate(Delegate.CPU)
            .build()
        HandLandmarker.createFromOptions(
            contexto,
            HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(opcionesBase)
                .setRunningMode(RunningMode.LIVE_STREAM)
                // Una sola mano: el abecedario dactilológico es de una mano.
                .setNumHands(1)
                .setMinHandDetectionConfidence(CONFIANZA_MINIMA)
                .setMinHandPresenceConfidence(CONFIANZA_MINIMA)
                .setMinTrackingConfidence(CONFIANZA_MINIMA)
                .setResultListener { resultado, _ -> alLlegarResultado(resultado) }
                .setErrorListener { error -> Log.e(ETIQUETA, "MediaPipe falló", error) }
                .build(),
        )
    } catch (e: Exception) {
        throw ErrorDeCamara("No se pudo inicializar MediaPipe con assets/$MODELO", e)
    }

    /** Se llama desde el hilo del ImageAnalysis de CameraX. Cierra el [imagen] siempre. */
    fun analizar(imagen: ImageProxy, esCamaraFrontal: Boolean) {
        try {
            val bitmap = aBitmap(imagen, esCamaraFrontal)
            // Latido: una línea por segundo que demuestra que la cadena está viva. Un frame sin
            // mano no llega al clasificador, así que sin esto "MediaPipe no ve nada" y "el log no
            // funciona" se veían igual: silencio.
            registro.registrarFrameDeCamara(
                ancho = bitmap.width,
                alto = bitmap.height,
                rotacion = imagen.imageInfo.rotationDegrees,
            )
            proporcionDeImagen = bitmap.width.toFloat() / bitmap.height
            inicioDeDeteccion = System.nanoTime()
            // detectAsync exige milisegundos estrictamente crecientes.
            detector.detectAsync(BitmapImageBuilder(bitmap).build(), SystemClock.uptimeMillis())
        } catch (e: Exception) {
            Log.e(ETIQUETA, "No se pudo analizar el frame", e)
        } finally {
            imagen.close()
        }
    }

    /**
     * Antes esto copiaba el plano 0 con copyPixelsFromBuffer sobre un bitmap reutilizado. Eso da
     * por supuesto que cada fila ocupa exactamente ancho x 4 bytes, y CameraX puede añadir
     * relleno al final de cada fila según el dispositivo. Cuando lo añade, la imagen le llega a
     * MediaPipe inclinada, sin que salte ninguna excepción, y la detección se va al traste solo
     * en algunos teléfonos. toBitmap() de CameraX respeta el rowStride.
     */
    private fun aBitmap(imagen: ImageProxy, esCamaraFrontal: Boolean): Bitmap {
        val origen = imagen.toBitmap()
        val transformacion = Matrix().apply {
            postRotate(imagen.imageInfo.rotationDegrees.toFloat())
            // El espejado que MediaPipe da por supuesto en la cámara frontal.
            if (esCamaraFrontal) postScale(-1f, 1f, origen.width / 2f, origen.height / 2f)
        }
        return Bitmap.createBitmap(
            origen, 0, 0, origen.width, origen.height, transformacion, true,
        )
    }

    private fun alLlegarResultado(resultado: HandLandmarkerResult) {
        val milisegundos = (System.nanoTime() - inicioDeDeteccion) / 1_000_000
        val puntos = resultado.landmarks().firstOrNull()

        if (puntos.isNullOrEmpty() || puntos.size != NormalizadorDeLandmarks.PUNTOS) {
            registro.registrarResultadoDeMediaPipe(huboMano = false)
            alDetectar(FrameDeLandmarks.SIN_MANO)
            return
        }
        registro.registrarResultadoDeMediaPipe(huboMano = true)

        val categoria = resultado.handedness().firstOrNull()?.firstOrNull()
        val handedness = categoria?.categoryName() ?: "Right"

        val crudos = FloatArray(NormalizadorDeLandmarks.VALORES)
        val enImagen = FloatArray(NormalizadorDeLandmarks.PUNTOS * 2)
        puntos.forEachIndexed { indice, punto ->
            crudos[indice * 3] = punto.x()
            crudos[indice * 3 + 1] = punto.y()
            crudos[indice * 3 + 2] = punto.z()
            enImagen[indice * 2] = punto.x()
            enImagen[indice * 2 + 1] = punto.y()
        }

        val normalizados = NormalizadorDeLandmarks.normalizar(
            puntosCrudos = crudos,
            esManoIzquierda = handedness == HANDEDNESS_IZQUIERDA,
        )

        registro.registrarDeteccion(
            milisegundos = milisegundos,
            handedness = handedness,
            puntuacion = categoria?.score() ?: -1f,
            escala = normalizados.escala,
            espejado = normalizados.espejado,
        )

        alDetectar(
            FrameDeLandmarks(
                landmarks = normalizados.valores,
                handedness = handedness,
                milisegundosDeDeteccion = milisegundos,
                puntosEnImagen = enImagen,
                proporcionDeImagen = proporcionDeImagen,
                timestampMs = resultado.timestampMs(),
            ),
        )
    }

    fun cerrar() {
        runCatching { detector.close() }
    }

    private companion object {
        const val ETIQUETA = "SignaCO_Debug"
        const val MODELO = "hand_landmarker.task"
        const val CONFIANZA_MINIMA = 0.5f
        const val HANDEDNESS_IZQUIERDA = "Left"
    }
}

/** La cámara o el detector no se pudieron inicializar. La pantalla lo muestra y deja reintentar. */
class ErrorDeCamara(mensaje: String, causa: Throwable? = null) : Exception(mensaje, causa)
