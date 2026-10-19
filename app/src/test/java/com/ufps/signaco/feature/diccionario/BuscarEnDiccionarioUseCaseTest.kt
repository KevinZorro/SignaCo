package com.ufps.signaco.feature.diccionario

import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.fakes.FakeSenaRepository
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.diccionario.domain.usecase.BuscarEnDiccionarioUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuscarEnDiccionarioUseCaseTest {

    private val senas = listOf(
        senaDePrueba("A", orden = 1),
        senaDePrueba("B", orden = 2),
        senaDePrueba("NN", orden = 3, disponible = false),
    )

    private val caso = BuscarEnDiccionarioUseCase(FakeSenaRepository(senas))

    @Test
    fun `sin texto devuelve el catalogo completo`() = runTest {
        val resultado: List<Sena> = caso("").first()

        assertEquals(3, resultado.size)
    }

    @Test
    fun `el diccionario si muestra las senas fuera del MVP`() = runTest {
        val resultado = caso("").first()

        assertTrue(resultado.any { it.id == "NN" })
    }

    @Test
    fun `filtra por letra sin importar mayusculas`() = runTest {
        val resultado = caso("b").first()

        assertEquals(listOf("B"), resultado.map { it.id })
    }

    @Test
    fun `filtra por el nombre de la sena`() = runTest {
        val resultado = caso("letra b").first()

        assertEquals(listOf("B"), resultado.map { it.id })
    }

    @Test
    fun `los espacios sobrantes no rompen la busqueda`() = runTest {
        val resultado = caso("  B  ").first()

        assertEquals(listOf("B"), resultado.map { it.id })
    }

    @Test
    fun `una consulta sin coincidencias devuelve lista vacia`() = runTest {
        val resultado = caso("zzz").first()

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun `el filtro de categoria deja solo las senas de esa categoria`() = runTest {
        val conConsonante = senas + senaDePrueba("C", orden = 4, categoria = Categoria.CONSONANTES)
        val caso = BuscarEnDiccionarioUseCase(FakeSenaRepository(conConsonante))

        val resultado = caso("", Categoria.CONSONANTES).first()

        assertEquals(listOf("C"), resultado.map { it.id })
    }
}
