package com.ufps.signaco.feature.lecciones

import com.ufps.signaco.fakes.FakeLeccionRepository
import com.ufps.signaco.fakes.leccionDePrueba
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerLeccionDeSenaUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObtenerLeccionDeSenaUseCaseTest {

    private val caso = ObtenerLeccionDeSenaUseCase(
        FakeLeccionRepository(
            listOf(
                leccionDePrueba("consonantes", listOf(senaDePrueba("B")), orden = 2),
                leccionDePrueba("vocales", listOf(senaDePrueba("A"), senaDePrueba("NN", disponible = false)), orden = 1),
            ),
        ),
    )

    @Test
    fun `devuelve la leccion donde se practica la sena`() = runTest {
        assertEquals("consonantes", caso("B").first())
    }

    @Test
    fun `una sena fuera del vocabulario no tiene leccion para practicar`() = runTest {
        assertNull(caso("NN").first())
    }
}
