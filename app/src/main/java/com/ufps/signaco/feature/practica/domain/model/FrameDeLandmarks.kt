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
    /**
     * Los 21 puntos SIN normalizar, como (x, y) entre 0 y 1 sobre la imagen ya rotada y
     * espejada. Solo sirven para dibujar la mano sobre la cámara; el modelo nunca los ve.
     */
    val puntosEnImagen: FloatArray = FloatArray(0),
    /** Ancho entre alto de esa imagen, para encajar los puntos en la vista previa. */
    val proporcionDeImagen: Float = 0f,
    /** Marca de tiempo en milisegundos en la que se capturó o procesó el frame. */
    val timestampMs: Long = 0L,
) {
    val hayMano: Boolean get() = landmarks.isNotEmpty()

    companion object {
        val SIN_MANO = FrameDeLandmarks(FloatArray(0))
    }
}
