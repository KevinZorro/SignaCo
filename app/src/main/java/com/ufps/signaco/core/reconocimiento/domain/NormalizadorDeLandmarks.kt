package com.ufps.signaco.core.reconocimiento.domain

import kotlin.math.abs
import kotlin.math.sqrt

/** Resultado de normalizar un frame, con los datos que el volcado de depuración necesita. */
class LandmarksNormalizados(
    val valores: FloatArray,
    val escala: Float,
    val espejado: Boolean,
)

/**
 * PORTADO DEL SPIKE. La aritmética y el orden de las operaciones son los del spike, que está
 * validado contra el notebook de entrenamiento. NO REESCRIBIR "mejorándola": si difiere aunque
 * sea en el orden, el modelo devuelve basura sin lanzar ninguna excepción.
 *
 * Los cinco pasos, en este orden:
 *  1. Restar a todos los puntos la posición del landmark 0 (la muñeca).
 *  2. Calcular la escala como la norma euclidiana máxima entre los puntos ya trasladados.
 *  3. Dividir todos los puntos por esa escala.
 *  4. Si la mano es izquierda, multiplicar la coordenada X por −1.
 *  5. Aplanar como x0,y0,z0,...,x20,y20,z20.
 *
 * Diferencia con el spike, y es de empaquetado, no de matemática: allí la escala, el
 * handedness y el espejado quedaban en variables @Volatile del objeto. Aquí se devuelven en
 * [LandmarksNormalizados]. El original compartía ese estado entre hilos y entre frames, lo que
 * en la app haría que el log de un frame mostrara los datos de otro.
 *
 * Kotlin puro: sin Android y sin MediaPipe, para que sea comprobable con pruebas unitarias.
 * Quien conoce a MediaPipe es [com.ufps.signaco.feature.practica.data.camara.AnalizadorDeManosMediaPipe].
 */
object NormalizadorDeLandmarks {

    /** Puntos que entrega MediaPipe HandLandmarker por mano. */
    const val PUNTOS = 21

    /** Valores que espera el modelo: 21 puntos x (x, y, z). */
    const val VALORES = PUNTOS * 3

    /**
     * @param puntosCrudos los 63 valores tal como los entrega MediaPipe, sin normalizar.
     * @param esManoIzquierda handedness == "Left" según MediaPipe.
     */
    fun normalizar(puntosCrudos: FloatArray, esManoIzquierda: Boolean): LandmarksNormalizados {
        require(puntosCrudos.size == VALORES) {
            "Se esperaban $VALORES valores y llegaron ${puntosCrudos.size}"
        }

        // 1. Traslación al origen usando la muñeca (landmark 0).
        val munecaX = puntosCrudos[0]
        val munecaY = puntosCrudos[1]
        val munecaZ = puntosCrudos[2]
        val trasladados = FloatArray(VALORES)
        for (punto in 0 until PUNTOS) {
            trasladados[punto * 3] = puntosCrudos[punto * 3] - munecaX
            trasladados[punto * 3 + 1] = puntosCrudos[punto * 3 + 1] - munecaY
            trasladados[punto * 3 + 2] = puntosCrudos[punto * 3 + 2] - munecaZ
        }

        // 2. La escala es la norma euclidiana MÁXIMA entre todos los puntos trasladados.
        var normaMaxima = 0f
        for (punto in 0 until PUNTOS) {
            val x = trasladados[punto * 3]
            val y = trasladados[punto * 3 + 1]
            val z = trasladados[punto * 3 + 2]
            val norma = sqrt(x * x + y * y + z * z)
            if (norma > normaMaxima) normaMaxima = norma
        }

        // 3. Dividir por la escala. 4. Espejar X si la mano es izquierda.
        val factor = if (normaMaxima > 0f) normaMaxima else 1f
        val valores = FloatArray(VALORES)
        for (punto in 0 until PUNTOS) {
            val x = trasladados[punto * 3] / factor
            // 5. Aplanado x,y,z por punto.
            valores[punto * 3] = if (esManoIzquierda) -x else x
            valores[punto * 3 + 1] = trasladados[punto * 3 + 1] / factor
            valores[punto * 3 + 2] = trasladados[punto * 3 + 2] / factor
        }

        return LandmarksNormalizados(
            valores = valores,
            escala = normaMaxima,
            espejado = esManoIzquierda,
        )
    }

    /**
     * DIAGNÓSTICO TEMPORAL. No forma parte de [normalizar] ni la modifica.
     *
     * MediaPipe no devuelve píxeles: devuelve `x` dividido entre el ANCHO de la imagen y `y`
     * dividido entre el ALTO. Con una imagen cuadrada los dos ejes quedan en la misma unidad;
     * con una imagen 3:4 el eje `y` sale comprimido un 25 % respecto a `x`. Trasladar a la
     * muñeca y dividir por la norma máxima quita el tamaño, pero no quita esa deformación: el
     * modelo vería una mano achatada en todos los frames.
     *
     * Esta función pasa `y` a unidades de ancho, que es lo que habría visto el entrenamiento si
     * corrió sobre recortes cuadrados. `z` ya viene en la escala de `x` según MediaPipe, así que
     * no se toca.
     *
     * Se aplica ANTES de [normalizar], sobre los valores crudos.
     */
    fun corregirRelacionDeAspecto(puntosCrudos: FloatArray, ancho: Int, alto: Int): FloatArray {
        require(puntosCrudos.size == VALORES) {
            "Se esperaban $VALORES valores y llegaron ${puntosCrudos.size}"
        }
        if (ancho <= 0 || alto <= 0 || ancho == alto) return puntosCrudos.copyOf()

        val factor = alto.toFloat() / ancho.toFloat()
        val salida = puntosCrudos.copyOf()
        for (punto in 0 until PUNTOS) {
            salida[punto * 3 + 1] = puntosCrudos[punto * 3 + 1] * factor
        }
        return salida
    }

    /**
     * Estadísticos por eje del vector normalizado. Sirven para detectar de un vistazo una
     * convención invertida: con la mano vertical y los dedos hacia arriba, los "y" normalizados
     * deben ser mayoritariamente NEGATIVOS, porque en MediaPipe la y de imagen crece hacia abajo.
     */
    fun estadisticasPorEje(valores: FloatArray): String {
        val nombres = arrayOf("x", "y", "z")
        val texto = StringBuilder()
        for (eje in 0..2) {
            var minimo = Float.MAX_VALUE
            var maximo = -Float.MAX_VALUE
            var suma = 0f
            var negativos = 0
            for (punto in 0 until PUNTOS) {
                val valor = valores[punto * 3 + eje]
                if (valor < minimo) minimo = valor
                if (valor > maximo) maximo = valor
                suma += valor
                if (valor < 0f) negativos++
            }
            texto.append(
                "%s[min=%.3f max=%.3f media=%.3f neg=%d/%d] "
                    .format(nombres[eje], minimo, maximo, suma / PUNTOS, negativos, PUNTOS),
            )
        }
        var maximoAbsoluto = 0f
        valores.forEach { if (abs(it) > maximoAbsoluto) maximoAbsoluto = abs(it) }
        return texto.append("maxAbs=%.3f".format(maximoAbsoluto)).toString()
    }
}
