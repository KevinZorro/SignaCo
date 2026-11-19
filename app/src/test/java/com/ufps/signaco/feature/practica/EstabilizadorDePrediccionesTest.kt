package com.ufps.signaco.feature.practica

import com.ufps.signaco.feature.practica.domain.EstabilizadorDePredicciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** RF07: suavizado temporal por frames consecutivos o ventana de tiempo con votación por mayoría. */
class EstabilizadorDePrediccionesTest {

    @Test
    fun `modo legacy - no confirma nada antes de completar los k frames`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 0L)

        assertNull(estabilizador.registrar("A"))
        assertNull(estabilizador.registrar("A"))
    }

    @Test
    fun `modo legacy - confirma al llegar al k-esimo frame igual`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 0L)

        estabilizador.registrar("A")
        estabilizador.registrar("A")

        assertEquals("A", estabilizador.registrar("A"))
    }

    @Test
    fun `modo legacy - no vuelve a confirmar mientras se sostiene la misma sena`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 0L)
        repeat(3) { estabilizador.registrar("A") }

        assertNull(estabilizador.registrar("A"))
        assertNull(estabilizador.registrar("A"))
    }

    @Test
    fun `modo legacy - una prediccion distinta rompe la racha`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 0L)

        estabilizador.registrar("A")
        estabilizador.registrar("B")
        assertNull(estabilizador.registrar("A"))
        assertNull(estabilizador.registrar("A"))
        assertEquals("A", estabilizador.registrar("A"))
    }

    @Test
    fun `modo legacy - volver a una sena ya confirmada la confirma de nuevo`() {
        val estabilizador = EstabilizadorDePredicciones(k = 2, ventanaMs = 0L)
        estabilizador.registrar("A")
        assertEquals("A", estabilizador.registrar("A"))

        estabilizador.registrar("B")
        assertEquals("B", estabilizador.registrar("B"))

        estabilizador.registrar("A")
        assertEquals("A", estabilizador.registrar("A"))
    }

    @Test
    fun `modo legacy - reiniciar borra la racha y el pestillo`() {
        val estabilizador = EstabilizadorDePredicciones(k = 2, ventanaMs = 0L)
        estabilizador.registrar("A")
        assertEquals("A", estabilizador.registrar("A"))

        estabilizador.reiniciar()

        assertEquals(0, estabilizador.avance)
        estabilizador.registrar("A")
        assertEquals("A", estabilizador.registrar("A"))
    }

    @Test
    fun `el avance cuenta los frames consecutivos iguales`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 0L)

        estabilizador.registrar("A")
        assertEquals(1, estabilizador.avance)
        estabilizador.registrar("A")
        assertEquals(2, estabilizador.avance)
        estabilizador.registrar("B")
        assertEquals(1, estabilizador.avance)
    }

    @Test
    fun `con k igual a uno confirma en el primer frame`() {
        val estabilizador = EstabilizadorDePredicciones(k = 1, ventanaMs = 0L)

        assertEquals("A", estabilizador.registrar("A"))
    }

    @Test
    fun `una k invalida se rechaza al construir`() {
        val error = runCatching { EstabilizadorDePredicciones(k = 0) }.exceptionOrNull()

        assertEquals(IllegalArgumentException::class, error!!::class)
    }

    @Test
    fun `fraccionMinima invalida se rechaza al construir con ventana mayor a cero`() {
        val errorMenorOIgualA05 = runCatching {
            EstabilizadorDePredicciones(k = 3, ventanaMs = 1_000L, fraccionMinima = 0.5f)
        }.exceptionOrNull()
        assertEquals(IllegalArgumentException::class, errorMenorOIgualA05!!::class)

        val errorMayorA1 = runCatching {
            EstabilizadorDePredicciones(k = 3, ventanaMs = 1_000L, fraccionMinima = 1.1f)
        }.exceptionOrNull()
        assertEquals(IllegalArgumentException::class, errorMayorA1!!::class)
    }

    @Test
    fun `ventana temporal - a 999 ms no confirma y a 1000 ms si confirma`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 1_000L)

        assertNull(estabilizador.registrar("A", coincidencia = 0.8f, timestampMs = 0L))
        assertNull(estabilizador.registrar("A", coincidencia = 0.8f, timestampMs = 500L))
        assertNull(estabilizador.registrar("A", coincidencia = 0.8f, timestampMs = 999L))

        assertEquals("A", estabilizador.registrar("A", coincidencia = 0.8f, timestampMs = 1_000L))
    }

    @Test
    fun `ventana temporal - pestillo no vuelve a confirmar mientras se sostiene`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 1_000L)

        estabilizador.registrar("A", timestampMs = 0L)
        estabilizador.registrar("A", timestampMs = 500L)
        assertEquals("A", estabilizador.registrar("A", timestampMs = 1_000L))

        // Frames siguientes dentro del mismo sostén
        assertNull(estabilizador.registrar("A", timestampMs = 1_050L))
        assertNull(estabilizador.registrar("A", timestampMs = 1_100L))
    }

    @Test
    fun `ventana temporal - un frame distinto aislado no reinicia y confirma por mayoria`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 1_000L, fraccionMinima = 0.8f)

        // 10 frames de "A" y solo 1 de "B" aislado a los 500 ms (10/11 = 90.9% >= 80%)
        estabilizador.registrar("A", timestampMs = 0L)
        estabilizador.registrar("A", timestampMs = 100L)
        estabilizador.registrar("A", timestampMs = 200L)
        estabilizador.registrar("A", timestampMs = 300L)
        estabilizador.registrar("A", timestampMs = 400L)
        estabilizador.registrar("B", timestampMs = 500L) // ruido aislado
        estabilizador.registrar("A", timestampMs = 600L)
        estabilizador.registrar("A", timestampMs = 700L)
        estabilizador.registrar("A", timestampMs = 800L)
        estabilizador.registrar("A", timestampMs = 900L)

        assertEquals("A", estabilizador.registrar("A", timestampMs = 1_000L))
    }

    @Test
    fun `ventana temporal - con 25 por ciento de frames distintos no confirma`() {
        val estabilizador = EstabilizadorDePredicciones(k = 4, ventanaMs = 1_000L, fraccionMinima = 0.8f)

        // 3 frames de "A" y 1 de "B" (3/4 = 75% < 80%)
        estabilizador.registrar("A", timestampMs = 0L)
        estabilizador.registrar("A", timestampMs = 300L)
        estabilizador.registrar("B", timestampMs = 600L)

        assertNull(estabilizador.registrar("A", timestampMs = 1_000L))
    }

    @Test
    fun `ventana temporal - borde exacto de 80 por ciento confirma con 4 de 5 y no con 3 de 5`() {
        val est4de5 = EstabilizadorDePredicciones(k = 5, ventanaMs = 1_000L, fraccionMinima = 0.8f)
        est4de5.registrar("A", timestampMs = 0L)
        est4de5.registrar("A", timestampMs = 250L)
        est4de5.registrar("A", timestampMs = 500L)
        est4de5.registrar("B", timestampMs = 750L)
        // 4 de 5 es exactamente 80%
        assertEquals("A", est4de5.registrar("A", timestampMs = 1_000L))

        val est3de5 = EstabilizadorDePredicciones(k = 5, ventanaMs = 1_000L, fraccionMinima = 0.8f)
        est3de5.registrar("A", timestampMs = 0L)
        est3de5.registrar("A", timestampMs = 250L)
        est3de5.registrar("B", timestampMs = 500L)
        est3de5.registrar("B", timestampMs = 750L)
        // 3 de 5 es 60% < 80%
        assertNull(est3de5.registrar("A", timestampMs = 1_000L))
    }

    @Test
    fun `ventana temporal - menos de k muestras no confirma aunque pase un segundo`() {
        val estabilizador = EstabilizadorDePredicciones(k = 5, ventanaMs = 1_000L)

        // Solo llegan 2 muestras (2 < 5)
        estabilizador.registrar("A", timestampMs = 0L)

        assertNull(estabilizador.registrar("A", timestampMs = 1_000L))
    }

    @Test
    fun `ventana temporal - pausa mayor que la ventana reinicia el temporizador`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 1_000L)

        estabilizador.registrar("A", timestampMs = 0L)
        estabilizador.registrar("A", timestampMs = 200L)

        // Salto de 1.100 ms (> 1.000 ms) entre frames consecutivos
        assertNull(estabilizador.registrar("A", timestampMs = 1_300L))
        // Al reiniciar en 1.300 ms, para confirmar necesita llegar a 2.300 ms
        assertNull(estabilizador.registrar("A", timestampMs = 1_800L))
        assertEquals("A", estabilizador.registrar("A", timestampMs = 2_300L))
    }

    @Test
    fun `ventana temporal - timestamp que retrocede reinicia el temporizador`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 1_000L)

        estabilizador.registrar("A", timestampMs = 500L)
        estabilizador.registrar("A", timestampMs = 700L)

        // Timestamp retrocede a 400L
        assertNull(estabilizador.registrar("A", timestampMs = 400L))
        assertNull(estabilizador.registrar("A", timestampMs = 900L))
        assertEquals("A", estabilizador.registrar("A", timestampMs = 1_400L))
    }

    @Test
    fun `ventana temporal - reiniciar limpia muestras y pestillo`() {
        val estabilizador = EstabilizadorDePredicciones(k = 2, ventanaMs = 1_000L)

        estabilizador.registrar("A", timestampMs = 0L)
        assertEquals("A", estabilizador.registrar("A", timestampMs = 1_000L))

        estabilizador.reiniciar()

        assertEquals(0, estabilizador.avance)
        assertNull(estabilizador.registrar("A", timestampMs = 2_000L))
        assertEquals("A", estabilizador.registrar("A", timestampMs = 3_000L))
    }

    @Test
    fun `promedia la coincidencia sobre las muestras recientes de la ventana de 1 segundo`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 1_000L)

        estabilizador.registrar("A", coincidencia = 0.60f, timestampMs = 0L)
        estabilizador.registrar("A", coincidencia = 0.80f, timestampMs = 200L)
        estabilizador.registrar("A", coincidencia = 1.00f, timestampMs = 400L)

        // Promedio: (0.60 + 0.80 + 1.00) / 3 = 0.80
        assertEquals(0.80f, estabilizador.coincidenciaPromedio, 0.001f)
    }

    @Test
    fun `desecha muestras mas viejas que la ventana de 1 segundo para el promedio`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3, ventanaMs = 1_000L)

        estabilizador.registrar("A", coincidencia = 0.20f, timestampMs = 0L)
        estabilizador.registrar("A", coincidencia = 0.80f, timestampMs = 1_100L)
        estabilizador.registrar("A", coincidencia = 0.90f, timestampMs = 1_200L)

        // La muestra de t=0L quedó fuera de los últimos 1.000 ms respecto a 1_200L
        // Promedio de las muestras vigentes: (0.80 + 0.90) / 2 = 0.85
        assertEquals(0.85f, estabilizador.coincidenciaPromedio, 0.001f)
    }
}
