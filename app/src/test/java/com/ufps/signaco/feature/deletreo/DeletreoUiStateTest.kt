package com.ufps.signaco.feature.deletreo

import com.ufps.signaco.feature.deletreo.presentation.DeletreoUiState
import com.ufps.signaco.feature.deletreo.presentation.PalabraDeletreada
import org.junit.Assert.assertEquals
import org.junit.Test

class DeletreoUiStateTest {

    private fun palabra(letras: String, sugerida: String?) =
        PalabraDeletreada(letras = letras, sugerida = sugerida, otras = emptyList(), ambigua = false)

    @Test fun `sin nada escrito la frase es vacia`() {
        assertEquals("", DeletreoUiState().frase)
    }

    @Test fun `la palabra cerrada con espacio no desaparece`() {
        // MAMA + gesto de espacio: las letras en curso quedan vacías y la palabra sigue a la vista
        val s = DeletreoUiState(palabras = listOf(palabra("M A M A", "MAMA")), letrasEnCurso = "")
        assertEquals("MAMA", s.frase)
    }

    @Test fun `la frase junta palabras terminadas y letras en curso`() {
        val s = DeletreoUiState(palabras = listOf(palabra("M A M A", "MAMA")), letrasEnCurso = "P A")
        assertEquals("MAMA PA", s.frase)
    }

    @Test fun `una palabra sin coincidencia se muestra con sus letras`() {
        val s = DeletreoUiState(palabras = listOf(palabra("X U Q", null)))
        assertEquals("XUQ", s.frase)
    }

    @Test fun `terminar palabra celebra solo si hay algo escrito`() {
        assertEquals(false, DeletreoUiState().celebrar().celebrando)
        assertEquals(false, DeletreoUiState().puedeTerminar)
        val s = DeletreoUiState(palabras = listOf(palabra("M A M A", "MAMA")))
        assertEquals(true, s.puedeTerminar)
        assertEquals(true, s.celebrar().celebrando)
    }

    @Test fun `durante la celebracion la frase sigue a la vista y no se puede terminar de nuevo`() {
        val s = DeletreoUiState(palabras = listOf(palabra("M A M A", "MAMA"))).celebrar()
        assertEquals("MAMA", s.frase)
        assertEquals(false, s.puedeTerminar)
    }

    @Test fun `al acabar el confeti se borra lo deletreado y se conserva el resto`() {
        val s = DeletreoUiState(
            permisoConcedido = true,
            cargando = false,
            palabras = listOf(palabra("M A M A", "MAMA")),
            letrasEnCurso = "P A",
        ).celebrar().limpio()
        assertEquals("", s.frase)
        assertEquals(false, s.celebrando)
        assertEquals(true, s.permisoConcedido)
        assertEquals(false, s.cargando)
    }
}
