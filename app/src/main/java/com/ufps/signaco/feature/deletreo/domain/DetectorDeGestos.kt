package com.ufps.signaco.feature.deletreo.domain

import kotlin.math.max
import kotlin.math.sqrt

/** Etiquetas de los gestos; viajan por la misma máquina de sostener y soltar que las letras. */
object Gestos {
    const val BORRAR = "@BORRAR"
    const val ESPACIO = "@ESPACIO"

    fun esGesto(etiqueta: String) = etiqueta == BORRAR || etiqueta == ESPACIO
}

/**
 * Reconoce los gestos por geometría sobre los 63 floats normalizados (muñeca en el origen,
 * escala = norma máxima): el modelo no tiene clases para ellos. Réplica de lsc70/gestures.py.
 *
 * - BORRAR: mano abierta, cinco dedos extendidos y separados (el "5").
 * - ESPACIO: pulgar, índice y meñique extendidos con medio y anular doblados ("te quiero").
 *
 * Se consultan ANTES del clasificador. Ninguna letra tiene estas formas: en 11 590 frames de
 * letras, la mano abierta salta en 0.3 % y "te quiero" en 0.1 %, y como se exige sostener 1 s
 * seguido, esos aciertos sueltos no llegan a confirmarse.
 */
class DetectorDeGestos(private val config: ConfiguracionDeGestos = ConfiguracionDeGestos()) {

    /** @return [Gestos.BORRAR], [Gestos.ESPACIO] o null. */
    fun detectar(v: FloatArray): String? {
        if (v.size != 63) return null
        val indice = razon(v, 8, 6)
        val medio = razon(v, 12, 10)
        val anular = razon(v, 16, 14)
        val menique = razon(v, 20, 18)
        val ancho = distancia(v, 5, 17)
        val pulgarAfuera = distancia(v, 4, 5) / max(ancho, 1e-6f) >= config.pulgarAfueraMin
        val ext = config.extendidoMin
        if (pulgarAfuera && indice >= ext && menique >= ext &&
            medio <= config.dobladoMax && anular <= config.dobladoMax
        ) {
            return Gestos.ESPACIO
        }
        if (pulgarAfuera && indice >= ext && medio >= ext && anular >= ext && menique >= ext) {
            val apertura = (distancia(v, 8, 12) + distancia(v, 12, 16) + distancia(v, 16, 20)) / 3f
            if (apertura >= config.aperturaMin) return Gestos.BORRAR
        }
        return null
    }

    private fun razon(v: FloatArray, punta: Int, articulacion: Int): Float =
        distanciaAMunieca(v, punta) / max(distanciaAMunieca(v, articulacion), 1e-6f)

    private fun distanciaAMunieca(v: FloatArray, i: Int): Float {
        val dx = v[i * 3] - v[0]
        val dy = v[i * 3 + 1] - v[1]
        val dz = v[i * 3 + 2] - v[2]
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    private fun distancia(v: FloatArray, a: Int, b: Int): Float {
        val dx = v[a * 3] - v[b * 3]
        val dy = v[a * 3 + 1] - v[b * 3 + 1]
        val dz = v[a * 3 + 2] - v[b * 3 + 2]
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
