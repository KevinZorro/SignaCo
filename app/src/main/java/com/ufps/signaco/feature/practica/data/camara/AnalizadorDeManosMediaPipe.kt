package com.ufps.signaco.feature.practica.data.camara

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
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

    private var bitmapReutilizable: Bitmap? = null
    private var inicioDeDeteccion: Long = 0

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
            val bitmap = aBitmap(imagen, esCamaraFrontal) ?: return
            inicioDeDeteccion = System.nanoTime()
            detector.detectAsync(BitmapImageBuilder(bitmap).build(), imagen.imageInfo.timestamp)
        } catch (e: Exception) {
            Log.e(ETIQUETA, "No se pudo analizar el frame", e)
        } finally {
            imagen.close()
        }
    }

    private fun aBitmap(imagen: ImageProxy, esCamaraFrontal: Boolean): Bitmap? {
        val destino = bitmapReutilizable
            ?: Bitmap.createBitmap(imagen.width, imagen.height, Bitmap.Config.ARGB_8888)
                .also { bitmapReutilizable = it }
        if (destino.width != imagen.width || destino.height != imagen.height) return null

        imagen.planes.firstOrNull()?.buffer?.let { destino.copyPixelsFromBuffer(it) } ?: return null

        val transformacion = Matrix().apply {
            postRotate(imagen.imageInfo.rotationDegrees.toFloat())
            // El espejado que MediaPipe da por supuesto en la cámara frontal.
            if (esCamaraFrontal) postScale(-1f, 1f, imagen.width / 2f, imagen.height / 2f)
        }
        return Bitmap.createBitmap(
            destino, 0, 0, imagen.width, imagen.height, transformacion, true,
        )
    }

    private fun alLlegarResultado(resultado: HandLandmarkerResult) {
        val milisegundos = (System.nanoTime() - inicioDeDeteccion) / 1_000_000
        val puntos = resultado.landmarks().firstOrNull()

        if (puntos.isNullOrEmpty() || puntos.size != NormalizadorDeLandmarks.PUNTOS) {
            alDetectar(FrameDeLandmarks.SIN_MANO)
            return
        }

        val categoria = resultado.handedness().firstOrNull()?.firstOrNull()
        val handedness = categoria?.categoryName() ?: "Right"

        val crudos = FloatArray(NormalizadorDeLandmarks.VALORES)
        puntos.forEachIndexed { indice, punto ->
            crudos[indice * 3] = punto.x()
            crudos[indice * 3 + 1] = punto.y()
            crudos[indice * 3 + 2] = punto.z()
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
            ),
        )
    }

    fun cerrar() {
        runCatching { detector.close() }
        bitmapReutilizable = null
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
