package com.ufps.signaco.core.senas

import com.ufps.signaco.core.database.entity.SenaEntity
import com.ufps.signaco.core.reconocimiento.domain.Alfabeto
import com.ufps.signaco.core.senas.data.ReferenciasEnRecursos
import com.ufps.signaco.core.senas.data.aDominio
import com.ufps.signaco.core.senas.domain.ReferenciasVisuales
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.ReferenciaVisual
import com.ufps.signaco.fakes.senaDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** RF02: cada seña se asocia con su imagen por identificador, y sin imagen nada falla. */
class ReferenciasVisualesTest {

    private val referencias = object : ReferenciasVisuales {
        override fun de(senaId: String): ReferenciaVisual? =
            if (senaId == "A") ReferenciaVisual.Imagen(RECURSO_A) else null
    }

    @Test
    fun `la seña con imagen la recibe al pasar al dominio`() {
        val sena = entidad("A").aDominio(referencias)

        assertEquals(ReferenciaVisual.Imagen(RECURSO_A), sena.referencia)
        assertTrue(sena.tieneReferencia)
    }

    @Test
    fun `la seña sin imagen queda sin referencia y sin fallar`() {
        val sena = entidad("B").aDominio(referencias)

        assertNull(sena.referencia)
        assertFalse(sena.tieneReferencia)
    }

    @Test
    fun `una seña creada sin referencia no tiene imagen`() {
        assertFalse(senaDePrueba("A").tieneReferencia)
    }

    @Test
    fun `una seña que no existe no tiene imagen en los recursos`() {
        assertNull(ReferenciasEnRecursos().de("no_existe"))
    }

    @Test
    fun `cada imagen empaquetada corresponde a una letra del abecedario`() {
        ReferenciasEnRecursos.IMAGENES.keys.forEach { senaId ->
            assertTrue("$senaId no es una letra del abecedario", senaId in Alfabeto.LETRAS)
        }
    }

    @Test
    fun `cada imagen empaquetada se entrega como imagen`() {
        ReferenciasEnRecursos.IMAGENES.forEach { (senaId, recurso) ->
            assertEquals(ReferenciaVisual.Imagen(recurso), ReferenciasEnRecursos().de(senaId))
        }
    }

    private fun entidad(id: String) = SenaEntity(
        id = id,
        letra = id,
        nombre = "Letra $id",
        descripcion = "",
        categoria = Categoria.VOCALES.name,
        orden = 1,
        disponible = true,
    )

    private companion object {
        const val RECURSO_A = 42
    }
}
