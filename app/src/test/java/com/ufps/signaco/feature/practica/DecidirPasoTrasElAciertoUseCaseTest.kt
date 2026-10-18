package com.ufps.signaco.feature.practica

import com.ufps.signaco.feature.practica.domain.model.PasoTrasElAcierto
import com.ufps.signaco.feature.practica.domain.usecase.DecidirPasoTrasElAciertoUseCase
import com.ufps.signaco.fakes.senaDePrueba
import org.junit.Assert.assertEquals
import org.junit.Test

/** A dónde lleva el botón tras acertar, según se practique la lección o una letra suelta. */
class DecidirPasoTrasElAciertoUseCaseTest {

    private val decidir = DecidirPasoTrasElAciertoUseCase()
    private val vocales = listOf("A", "I", "O", "U").mapIndexed { i, id -> senaDePrueba(id, orden = i) }

    @Test
    fun `una letra suelta vuelve a la leccion aunque queden letras`() {
        assertEquals(
            PasoTrasElAcierto.VolverALaLeccion,
            decidir(vocales, senaActualId = "A", soloEstaSena = true),
        )
    }

    @Test
    fun `una letra suelta vuelve a la leccion aunque sea la ultima`() {
        assertEquals(
            PasoTrasElAcierto.VolverALaLeccion,
            decidir(vocales, senaActualId = "U", soloEstaSena = true),
        )
    }

    @Test
    fun `en la leccion completa pasa a la siguiente letra en orden`() {
        assertEquals(
            PasoTrasElAcierto.SiguienteSena("I"),
            decidir(vocales, senaActualId = "A", soloEstaSena = false),
        )
        assertEquals(
            PasoTrasElAcierto.SiguienteSena("U"),
            decidir(vocales, senaActualId = "O", soloEstaSena = false),
        )
    }

    @Test
    fun `en la leccion completa la ultima letra cierra la leccion`() {
        assertEquals(
            PasoTrasElAcierto.TerminarLeccion,
            decidir(vocales, senaActualId = "U", soloEstaSena = false),
        )
    }

    @Test
    fun `una leccion de una sola letra se cierra al acertarla`() {
        assertEquals(
            PasoTrasElAcierto.TerminarLeccion,
            decidir(listOf(senaDePrueba("A")), senaActualId = "A", soloEstaSena = false),
        )
    }

    @Test
    fun `si la letra actual no esta en la leccion se cierra en vez de fallar`() {
        assertEquals(
            PasoTrasElAcierto.TerminarLeccion,
            decidir(vocales, senaActualId = "Z", soloEstaSena = false),
        )
        assertEquals(
            PasoTrasElAcierto.TerminarLeccion,
            decidir(emptyList(), senaActualId = "", soloEstaSena = false),
        )
    }
}
