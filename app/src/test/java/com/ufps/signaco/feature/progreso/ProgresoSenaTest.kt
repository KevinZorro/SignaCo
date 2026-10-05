package com.ufps.signaco.feature.progreso

import com.ufps.signaco.feature.progreso.domain.model.EstadoDeSena
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgresoSenaTest {

    @Test
    fun `sin intentos la tasa de acierto es cero y no divide por cero`() {
        val progreso = ProgresoSena.vacio("A")

        assertEquals(0f, progreso.tasaDeAcierto, 0.0001f)
        assertFalse(progreso.dominada)
    }

    @Test
    fun `pocos aciertos no alcanzan para dominar aunque la tasa sea perfecta`() {
        val progreso = ProgresoSena("A", intentos = 3, aciertos = 3)

        assertEquals(1f, progreso.tasaDeAcierto, 0.0001f)
        assertFalse(progreso.dominada)
    }

    @Test
    fun `muchos aciertos con tasa baja tampoco alcanzan`() {
        val progreso = ProgresoSena("A", intentos = 20, aciertos = 6)

        assertTrue(progreso.aciertos >= ProgresoSena.ACIERTOS_PARA_DOMINAR)
        assertFalse(progreso.dominada)
    }

    @Test
    fun `con suficientes aciertos y buena tasa la sena queda dominada`() {
        val progreso = ProgresoSena("A", intentos = 7, aciertos = 6)

        assertTrue(progreso.dominada)
    }

    @Test
    fun `sin intentos la sena esta por aprender`() {
        assertEquals(EstadoDeSena.POR_APRENDER, ProgresoSena.vacio("A").estado)
    }

    @Test
    fun `con intentos pero sin dominar la sena esta practicada`() {
        assertEquals(EstadoDeSena.PRACTICADA, ProgresoSena("A", intentos = 3, aciertos = 1).estado)
    }

    @Test
    fun `una sena dominada no cuenta como practicada`() {
        assertEquals(EstadoDeSena.DOMINADA, ProgresoSena("A", intentos = 6, aciertos = 6).estado)
    }
}
