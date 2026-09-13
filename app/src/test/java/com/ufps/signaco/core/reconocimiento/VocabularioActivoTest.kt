package com.ufps.signaco.core.reconocimiento

import com.ufps.signaco.core.reconocimiento.domain.Alfabeto
import com.ufps.signaco.core.reconocimiento.domain.VocabularioActivo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VocabularioActivoTest {

    @Test
    fun `la app enseña 19 letras de las 27 que conoce el modelo`() {
        assertEquals(19, VocabularioActivo.LETRAS.size)
        assertEquals(27, Alfabeto.LETRAS.size)
    }

    @Test
    fun `las excluidas son las ocho documentadas`() {
        assertEquals(
            listOf("C", "E", "H", "K", "M", "N", "NN", "Z"),
            VocabularioActivo.EXCLUIDAS.sorted(),
        )
    }

    @Test
    fun `activas y excluidas cubren el alfabeto sin solaparse`() {
        assertEquals(
            Alfabeto.LETRAS.sorted(),
            (VocabularioActivo.LETRAS + VocabularioActivo.EXCLUIDAS).sorted(),
        )
    }

    @Test
    fun `toda letra activa pertenece al alfabeto del modelo`() {
        VocabularioActivo.LETRAS.forEach {
            assertTrue("$it no está en las etiquetas del modelo", it in Alfabeto.LETRAS)
        }
    }

    @Test
    fun `los indices activos apuntan a las letras correctas`() {
        val indices = VocabularioActivo.indicesActivosEn(Alfabeto.LETRAS)

        assertEquals(VocabularioActivo.LETRAS.size, indices.size)
        indices.forEach { assertTrue(VocabularioActivo.estaActiva(Alfabeto.LETRAS[it])) }
    }

    @Test
    fun `la enie y la zeta quedan fuera por ser señas dinamicas`() {
        assertFalse(VocabularioActivo.estaActiva("NN"))
        assertFalse(VocabularioActivo.estaActiva("Z"))
    }
}
