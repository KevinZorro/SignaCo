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
}
