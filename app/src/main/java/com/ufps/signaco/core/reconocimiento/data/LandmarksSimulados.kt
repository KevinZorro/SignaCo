package com.ufps.signaco.core.reconocimiento.data

import com.ufps.signaco.core.reconocimiento.domain.Alfabeto

/**
 * Codifica letras en arreglos de 63 floats para que [FakeSignClassifier] pueda devolver
 * resultados predecibles.
 *
 * Era el andamio de la pantalla de práctica antes de que existiera la cámara. Ahora que la
 * cámara es real, esto vive aquí como detalle interno del clasificador falso, que se conserva
 * para las pruebas unitarias y para desarrollar sin dispositivo.
 */
object LandmarksSimulados {

    /** Puntos que entrega MediaPipe HandLandmarker por mano. */
    const val PUNTOS = 21

    /** Valores que espera el modelo: 21 puntos x (x, y, z). */
    const val VALORES = PUNTOS * 3

    private const val MARCA_NO_RECONOCIDA = -1f

    /** Frame sin mano en cuadro. */
    fun manoFueraDeCuadro(): FloatArray = FloatArray(0)

    /** Frame con mano pero con una pose que no es ninguna letra. */
    fun senaNoReconocida(): FloatArray = FloatArray(VALORES) { MARCA_NO_RECONOCIDA }

    /** Frame que el clasificador falso leera como [letra]. */
    fun senaDe(letra: String): FloatArray {
        val indice = Alfabeto.indiceDe(letra)
        require(indice >= 0) { "Letra fuera del vocabulario: $letra" }
        return FloatArray(VALORES).also { it[0] = indice.toFloat() }
    }

    /** Lee la letra codificada en el frame, o null si el frame no codifica ninguna. */
    fun letraCodificadaEn(landmarks: FloatArray): String? {
        if (landmarks.size != VALORES) return null
        val indice = landmarks[0]
        if (indice < 0f) return null
        return Alfabeto.letraEn(indice.toInt())
    }
}
