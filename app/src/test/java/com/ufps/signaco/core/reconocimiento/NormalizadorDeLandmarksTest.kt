package com.ufps.signaco.core.reconocimiento

import com.ufps.signaco.core.reconocimiento.domain.NormalizadorDeLandmarks
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * La normalización es la pieza que, si se rompe, hace que el modelo devuelva basura sin lanzar
 * ninguna excepción. Estas pruebas fijan los cinco pasos y su orden.
 */
class NormalizadorDeLandmarksTest {

    /** 21 puntos con la muñeca en (0.5, 0.5, 0) y el resto donde diga [puntos]. */
    private fun manoDePrueba(vararg puntos: Triple<Float, Float, Float>): FloatArray {
        val crudos = FloatArray(NormalizadorDeLandmarks.VALORES)
        val todos = listOf(Triple(0.5f, 0.5f, 0f)) + puntos.toList()
        for (indice in 0 until NormalizadorDeLandmarks.PUNTOS) {
            val (x, y, z) = todos.getOrElse(indice) { Triple(0.5f, 0.5f, 0f) }
            crudos[indice * 3] = x
            crudos[indice * 3 + 1] = y
            crudos[indice * 3 + 2] = z
        }
        return crudos
    }

    @Test
    fun `la muñeca queda en el origen`() {
        val normalizados = NormalizadorDeLandmarks
            .normalizar(manoDePrueba(Triple(0.5f, 0.1f, 0f)), esManoIzquierda = false)

        assertEquals(0f, normalizados.valores[0], 1e-6f)
        assertEquals(0f, normalizados.valores[1], 1e-6f)
        assertEquals(0f, normalizados.valores[2], 1e-6f)
    }

    @Test
    fun `la escala es la norma euclidiana maxima y deja el punto mas lejano a distancia uno`() {
        // El punto más lejano queda a 0.4 de la muñeca.
        val normalizados = NormalizadorDeLandmarks
            .normalizar(manoDePrueba(Triple(0.5f, 0.1f, 0f)), esManoIzquierda = false)

        assertEquals(0.4f, normalizados.escala, 1e-5f)
        assertEquals(-1f, normalizados.valores[4], 1e-5f)

        var normaMaxima = 0f
        for (punto in 0 until NormalizadorDeLandmarks.PUNTOS) {
            val x = normalizados.valores[punto * 3]
            val y = normalizados.valores[punto * 3 + 1]
            val z = normalizados.valores[punto * 3 + 2]
            normaMaxima = maxOf(normaMaxima, sqrt(x * x + y * y + z * z))
        }
        assertEquals(1f, normaMaxima, 1e-5f)
    }

    @Test
    fun `la mano izquierda espeja la X y deja intactas la Y y la Z`() {
        val crudos = manoDePrueba(Triple(0.9f, 0.2f, 0.3f))

        val derecha = NormalizadorDeLandmarks.normalizar(crudos, esManoIzquierda = false)
        val izquierda = NormalizadorDeLandmarks.normalizar(crudos, esManoIzquierda = true)

        assertEquals(-derecha.valores[3], izquierda.valores[3], 1e-6f)
        assertEquals(derecha.valores[4], izquierda.valores[4], 1e-6f)
        assertEquals(derecha.valores[5], izquierda.valores[5], 1e-6f)
        assertTrue(izquierda.espejado)
    }

    @Test
    fun `el orden importa, primero se traslada y despues se escala`() {
        // Con la muñeca lejos del origen, escalar antes de trasladar daría otro resultado.
        val lejos = FloatArray(NormalizadorDeLandmarks.VALORES)
        for (punto in 0 until NormalizadorDeLandmarks.PUNTOS) {
            lejos[punto * 3] = 10f
            lejos[punto * 3 + 1] = 10f
        }
        lejos[3] = 12f
        lejos[4] = 10f

        val normalizados = NormalizadorDeLandmarks.normalizar(lejos, esManoIzquierda = false)

        // La escala sale de los puntos YA trasladados: la distancia es 2, no la norma de (10,10).
        assertEquals(2f, normalizados.escala, 1e-5f)
        assertEquals(1f, normalizados.valores[3], 1e-5f)
    }

    @Test
    fun `una mano con todos los puntos iguales no divide por cero`() {
        val normalizados = NormalizadorDeLandmarks
            .normalizar(manoDePrueba(), esManoIzquierda = false)

        assertEquals(0f, normalizados.escala, 1e-6f)
        assertTrue(normalizados.valores.all { it == 0f && !it.isNaN() })
    }

    @Test
    fun `un arreglo con un numero de valores distinto de 63 se rechaza`() {
        val error = runCatching {
            NormalizadorDeLandmarks.normalizar(FloatArray(60), esManoIzquierda = false)
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun `las estadisticas por eje delatan una convencion invertida`() {
        // Dedos hacia arriba: en MediaPipe la y crece hacia abajo, así que los y normalizados
        // deben ser negativos.
        val normalizados = NormalizadorDeLandmarks
            .normalizar(manoDePrueba(Triple(0.5f, 0.1f, 0f)), esManoIzquierda = false)

        val texto = NormalizadorDeLandmarks.estadisticasPorEje(normalizados.valores)

        assertTrue(texto.contains("y["))
        assertTrue(texto.contains("maxAbs="))
        assertTrue(normalizados.valores.filterIndexed { i, _ -> i % 3 == 1 }.any { abs(it) > 0f })
    }

    @Test
    fun `corregir el aspecto solo escala la y y la deja en unidades de ancho`() {
        val crudos = manoDePrueba(Triple(0.8f, 0.2f, 0.05f))

        val corregidos = NormalizadorDeLandmarks
            .corregirRelacionDeAspecto(crudos, ancho = 480, alto = 640)

        // x y z intactos, y multiplicada por alto/ancho = 1.333...
        assertEquals(crudos[3], corregidos[3], 1e-6f)
        assertEquals(crudos[5], corregidos[5], 1e-6f)
        assertEquals(crudos[4] * 640f / 480f, corregidos[4], 1e-6f)
        assertEquals(crudos[1] * 640f / 480f, corregidos[1], 1e-6f)
    }

    @Test
    fun `corregir el aspecto no hace nada si la imagen ya es cuadrada`() {
        val crudos = manoDePrueba(Triple(0.8f, 0.2f, 0.05f))

        val corregidos = NormalizadorDeLandmarks
            .corregirRelacionDeAspecto(crudos, ancho = 512, alto = 512)

        assertArrayEquals(crudos, corregidos, 1e-6f)
    }

    @Test
    fun `corregir el aspecto no toca el arreglo original`() {
        val crudos = manoDePrueba(Triple(0.8f, 0.2f, 0.05f))
        val copia = crudos.copyOf()

        NormalizadorDeLandmarks.corregirRelacionDeAspecto(crudos, ancho = 480, alto = 640)

        assertArrayEquals(copia, crudos, 0f)
    }

    @Test
    fun `corregir el aspecto cambia la forma que ve el modelo, no solo el tamaño`() {
        // El punto de la prueba: trasladar a la muñeca y dividir por la norma máxima quita el
        // TAMAÑO, no la deformación. Si fuera solo tamaño, los dos vectores normalizados
        // saldrían iguales y la corrección no serviría de nada.
        val crudos = manoDePrueba(Triple(0.8f, 0.2f, 0f), Triple(0.2f, 0.9f, 0f))

        val tal = NormalizadorDeLandmarks.normalizar(crudos, esManoIzquierda = false).valores
        val corregido = NormalizadorDeLandmarks.normalizar(
            NormalizadorDeLandmarks.corregirRelacionDeAspecto(crudos, ancho = 480, alto = 640),
            esManoIzquierda = false,
        ).valores

        assertTrue(tal.indices.any { abs(tal[it] - corregido[it]) > 1e-3f })
    }
}
