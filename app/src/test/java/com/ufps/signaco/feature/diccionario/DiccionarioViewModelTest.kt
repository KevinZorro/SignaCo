package com.ufps.signaco.feature.diccionario

import com.ufps.signaco.core.senas.domain.SenaRepository
import com.ufps.signaco.fakes.FakeSenaRepository
import com.ufps.signaco.fakes.ReglaDeDispatcherPrincipal
import com.ufps.signaco.fakes.SenaRepositoryQueFalla
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.diccionario.domain.usecase.BuscarEnDiccionarioUseCase
import com.ufps.signaco.feature.diccionario.presentation.DiccionarioViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DiccionarioViewModelTest {

    @get:Rule
    val reglaDeDispatcher = ReglaDeDispatcherPrincipal()

    private val senas = listOf(
        senaDePrueba("A", orden = 1),
        senaDePrueba("B", orden = 2),
        senaDePrueba("NN", orden = 3, disponible = false),
    )

    private fun crearViewModel(
        repositorio: SenaRepository = FakeSenaRepository(senas),
    ) = DiccionarioViewModel(BuscarEnDiccionarioUseCase(repositorio))

    @Test
    fun `sin busqueda muestra el catalogo completo`() = runTest {
        val estado = crearViewModel().uiState.first { !it.cargando }

        assertFalse(estado.hayError)
        assertEquals(3, estado.senas.size)
        assertFalse(estado.sinResultados)
    }

    @Test
    fun `la consulta filtra el catalogo`() = runTest {
        val viewModel = crearViewModel()

        viewModel.cambiarConsulta("b")
        val estado = viewModel.uiState.first { !it.cargando && it.consulta == "b" }

        assertEquals(listOf("B"), estado.senas.map { it.id })
    }

    @Test
    fun `una consulta sin resultados se marca como tal, no como error`() = runTest {
        val viewModel = crearViewModel()

        viewModel.cambiarConsulta("zzz")
        val estado = viewModel.uiState.first { !it.cargando && it.consulta == "zzz" }

        assertTrue(estado.sinResultados)
        assertFalse(estado.hayError)
    }

    @Test
    fun `si falla la carga el estado queda en error`() = runTest {
        val estado = crearViewModel(SenaRepositoryQueFalla()).uiState.first { !it.cargando }

        assertTrue(estado.hayError)
    }
}
