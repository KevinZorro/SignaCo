package com.ufps.signaco.core.database

import com.ufps.signaco.core.reconocimiento.domain.Alfabeto
import com.ufps.signaco.core.reconocimiento.domain.VocabularioActivo
import com.ufps.signaco.core.senas.domain.model.Categoria
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El catálogo se precarga una sola vez, al crear la base: si tiene un dato mal puesto, la
 * app arranca con datos malos y nadie se entera. Estas pruebas son de Kotlin puro, no
 * necesitan Room ni emulador.
 */
class CatalogoInicialTest {

    @Test
    fun `el catalogo trae las 27 letras del abecedario`() {
        assertEquals(27, CatalogoInicial.SENAS.size)
    }

    @Test
    fun `no hay identificadores repetidos`() {
        val ids = CatalogoInicial.SENAS.map { it.id }

        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `el orden va de 1 a 27 sin huecos ni repetidos`() {
        val ordenes = CatalogoInicial.SENAS.map { it.orden }.sorted()

        assertEquals((1..27).toList(), ordenes)
    }

    @Test
    fun `cada sena del catalogo es una etiqueta que el modelo conoce`() {
        val fueraDelAlfabeto = CatalogoInicial.SENAS.map { it.id } - Alfabeto.LETRAS.toSet()

        assertTrue("Señas que el modelo no conoce: $fueraDelAlfabeto", fueraDelAlfabeto.isEmpty())
    }

    @Test
    fun `las categorias guardadas se pueden leer de vuelta como enum`() {
        CatalogoInicial.SENAS.forEach { Categoria.valueOf(it.categoria) }
        CatalogoInicial.LECCIONES.forEach { Categoria.valueOf(it.categoria) }
    }

    @Test
    fun `las cinco vocales siguen en el catalogo aunque la E no se practique`() {
        val vocales = CatalogoInicial.SENAS.filter { it.categoria == Categoria.VOCALES.name }

        assertEquals(listOf("A", "E", "I", "O", "U"), vocales.map { it.id })
    }

    @Test
    fun `la disponibilidad del catalogo coincide con el vocabulario activo`() {
        val disponibles = CatalogoInicial.SENAS.filter { it.disponible }.map { it.id }.sorted()

        assertEquals(VocabularioActivo.LETRAS.sorted(), disponibles)
    }

    @Test
    fun `las senas excluidas siguen en el catalogo para el diccionario`() {
        val ids = CatalogoInicial.SENAS.map { it.id }

        VocabularioActivo.EXCLUIDAS.forEach { excluida ->
            assertTrue("Falta $excluida en el catálogo", excluida in ids)
        }
    }

    @Test
    fun `ninguna sena se queda sin nombre`() {
        val sinNombre = CatalogoInicial.SENAS.filter { it.nombre.isBlank() }

        assertTrue(sinNombre.isEmpty())
    }

    @Test
    fun `las cinco lecciones tienen identificadores distintos`() {
        val ids = CatalogoInicial.LECCIONES.map { it.id }

        assertEquals(5, ids.size)
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `las relaciones apuntan a lecciones y senas que existen`() {
        val lecciones = CatalogoInicial.LECCIONES.map { it.id }.toSet()
        val senas = CatalogoInicial.SENAS.map { it.id }.toSet()

        CatalogoInicial.RELACIONES.forEach { relacion ->
            assertTrue(
                "Lección inexistente: ${relacion.leccionId}",
                relacion.leccionId in lecciones,
            )
            assertTrue("Seña inexistente: ${relacion.senaId}", relacion.senaId in senas)
        }
    }

    @Test
    fun `ninguna leccion se queda vacia`() {
        val conSenas = CatalogoInicial.RELACIONES.map { it.leccionId }.toSet()

        assertEquals(CatalogoInicial.LECCIONES.map { it.id }.toSet(), conSenas)
    }

    @Test
    fun `cada sena practicable pertenece exactamente a una leccion`() {
        val practicables = CatalogoInicial.SENAS.filter { it.disponible }.map { it.id }.sorted()
        val enLecciones = CatalogoInicial.RELACIONES.map { it.senaId }

        assertEquals(enLecciones.size, enLecciones.distinct().size)
        assertEquals(practicables, enLecciones.sorted())
    }

    @Test
    fun `ninguna sena excluida aparece en una leccion`() {
        val enLecciones = CatalogoInicial.RELACIONES.map { it.senaId }.toSet()

        VocabularioActivo.EXCLUIDAS.forEach { excluida ->
            assertFalse("$excluida no debería estar en una lección", excluida in enLecciones)
        }
    }

    @Test
    fun `cada leccion tiene entre 3 y 5 senas, como pide el RF01`() {
        CatalogoInicial.RELACIONES.groupBy { it.leccionId }.forEach { (leccion, senas) ->
            assertTrue("$leccion tiene ${senas.size} señas", senas.size in 3..5)
        }
    }
}
