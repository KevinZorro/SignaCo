package com.ufps.signaco.feature.practica

import com.ufps.signaco.feature.practica.domain.EstabilizadorDePredicciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** RF07: una seña se confirma solo si se mantiene estable durante k frames consecutivos. */
class EstabilizadorDePrediccionesTest {

    @Test
    fun `no confirma nada antes de completar los k frames`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3)

        assertNull(estabilizador.registrar("A"))
        assertNull(estabilizador.registrar("A"))
    }

    @Test
    fun `confirma al llegar al k-esimo frame igual`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3)

        estabilizador.registrar("A")
        estabilizador.registrar("A")

        assertEquals("A", estabilizador.registrar("A"))
    }

    @Test
    fun `no vuelve a confirmar mientras se sostiene la misma sena`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3)
        repeat(3) { estabilizador.registrar("A") }

        // Sin este pestillo, a 30 fps se registraría un intento nuevo cada k frames.
        assertNull(estabilizador.registrar("A"))
        assertNull(estabilizador.registrar("A"))
    }

    @Test
    fun `una prediccion distinta rompe la racha`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3)

        estabilizador.registrar("A")
        estabilizador.registrar("B")
        assertNull(estabilizador.registrar("A"))
        assertNull(estabilizador.registrar("A"))
        assertEquals("A", estabilizador.registrar("A"))
    }

    @Test
    fun `volver a una sena ya confirmada la confirma de nuevo`() {
        val estabilizador = EstabilizadorDePredicciones(k = 2)
        estabilizador.registrar("A")
        assertEquals("A", estabilizador.registrar("A"))

        estabilizador.registrar("B")
        assertEquals("B", estabilizador.registrar("B"))

        estabilizador.registrar("A")
        assertEquals("A", estabilizador.registrar("A"))
    }

    @Test
    fun `reiniciar borra la racha y el pestillo`() {
        val estabilizador = EstabilizadorDePredicciones(k = 2)
        estabilizador.registrar("A")
        assertEquals("A", estabilizador.registrar("A"))

        estabilizador.reiniciar()

        assertEquals(0, estabilizador.avance)
        estabilizador.registrar("A")
        assertEquals("A", estabilizador.registrar("A"))
    }

    @Test
    fun `el avance cuenta los frames consecutivos iguales`() {
        val estabilizador = EstabilizadorDePredicciones(k = 3)

        estabilizador.registrar("A")
        assertEquals(1, estabilizador.avance)
        estabilizador.registrar("A")
        assertEquals(2, estabilizador.avance)
        estabilizador.registrar("B")
        assertEquals(1, estabilizador.avance)
    }

    @Test
    fun `con k igual a uno confirma en el primer frame`() {
        val estabilizador = EstabilizadorDePredicciones(k = 1)

        assertEquals("A", estabilizador.registrar("A"))
    }

    @Test
    fun `una k invalida se rechaza al construir`() {
        val error = runCatching { EstabilizadorDePredicciones(k = 0) }.exceptionOrNull()

        assertEquals(IllegalArgumentException::class, error!!::class)
    }
}
