package com.ufps.signaco.feature.lecciones

import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso
import com.ufps.signaco.feature.lecciones.domain.model.SenaConProgreso
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import com.ufps.signaco.fakes.leccionDePrueba
import com.ufps.signaco.fakes.senaDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** «Empezar práctica» arranca por la indicación de la primera letra que se puede practicar. */
class PrimeraSenaParaPracticarTest {

    @Test
    fun `arranca por la primera letra de la leccion`() {
        assertEquals("A", conSenas(senaDePrueba("A"), senaDePrueba("I")).primeraSenaParaPracticar)
    }

    @Test
    fun `salta las letras que no estan en el vocabulario`() {
        val leccion = conSenas(senaDePrueba("NN", disponible = false), senaDePrueba("O"))

        assertEquals("O", leccion.primeraSenaParaPracticar)
    }

    @Test
    fun `sin letras practicables no hay por donde empezar`() {
        assertNull(conSenas(senaDePrueba("NN", disponible = false)).primeraSenaParaPracticar)
        assertNull(conSenas().primeraSenaParaPracticar)
    }

    private fun conSenas(vararg senas: Sena) =
        LeccionConProgreso(
            leccion = leccionDePrueba("leccion", senas.toList()),
            senas = senas.map { SenaConProgreso(it, ProgresoSena.vacio(it.id)) },
            completada = false,
        )
}
