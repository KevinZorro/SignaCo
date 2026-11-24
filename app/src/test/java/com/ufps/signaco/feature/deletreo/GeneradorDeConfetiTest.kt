package com.ufps.signaco.feature.deletreo

import com.ufps.signaco.feature.deletreo.presentation.GeneradorDeConfeti
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneradorDeConfetiTest {

    private val piezas = GeneradorDeConfeti.generar(cantidad = 60, colores = 5)

    @Test fun `la misma semilla da las mismas piezas`() {
        assertEquals(piezas, GeneradorDeConfeti.generar(cantidad = 60, colores = 5))
        assertEquals(60, piezas.size)
    }

    @Test fun `al empezar todo esta sobre el borde superior`() {
        assertTrue(piezas.all { GeneradorDeConfeti.y(it, 0f) <= 0f })
    }

    @Test fun `al terminar todo salio por abajo`() {
        assertTrue(piezas.all { GeneradorDeConfeti.y(it, 1f) > 1f })
    }

    @Test fun `solo cae, sin rebotes`() {
        for (p in piezas) {
            var previa = GeneradorDeConfeti.y(p, 0f)
            for (i in 1..20) {
                val y = GeneradorDeConfeti.y(p, i / 20f)
                assertTrue(y >= previa)
                previa = y
            }
        }
    }

    @Test fun `los colores caen dentro de la paleta`() {
        assertTrue(piezas.all { it.color in 0 until 5 })
    }

    @Test fun `no se sale demasiado de los lados`() {
        for (p in piezas) for (i in 0..20) {
            val x = GeneradorDeConfeti.x(p, i / 20f)
            assertTrue(x > -0.3f && x < 1.3f)
        }
    }
}
