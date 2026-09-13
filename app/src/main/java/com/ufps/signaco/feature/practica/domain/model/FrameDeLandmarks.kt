package com.ufps.signaco.feature.practica.domain.model

/**
 * Un frame ya procesado por la capa de cámara: landmarks normalizados y listos para el modelo.
 *
 * El dominio nunca ve un frame de vídeo ni un tipo de MediaPipe. Tampoco se almacena imagen
 * alguna: solo estos 63 floats, que además se descartan al terminar el frame (RNF-06).
 */
class FrameDeLandmarks(
    /** 63 floats normalizados, o vacío si no se detectó mano. */
    val landmarks: FloatArray,
    val handedness: String = "?",
    val milisegundosDeDeteccion: Long = 0,
) {
    val hayMano: Boolean get() = landmarks.isNotEmpty()

    companion object {
        val SIN_MANO = FrameDeLandmarks(FloatArray(0))
    }
}
