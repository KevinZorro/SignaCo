package com.ufps.signaco.feature.lecciones

import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.fakes.FakeLeccionRepository
import com.ufps.signaco.fakes.FakeProgresoRepository
import com.ufps.signaco.fakes.FakeRachaRepository
import com.ufps.signaco.fakes.FakeSenaRepository
import com.ufps.signaco.fakes.LeccionRepositoryQueFalla
import com.ufps.signaco.fakes.ReglaDeDispatcherPrincipal
import com.ufps.signaco.fakes.RelojFijo
import com.ufps.signaco.fakes.leccionDePrueba
import com.ufps.signaco.fakes.senaDePrueba
import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import com.ufps.signaco.feature.lecciones.domain.usecase.CombinarLeccionConProgresoUseCase
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerCatalogoDeLeccionesUseCase
import com.ufps.signaco.feature.lecciones.presentation.catalogo.CatalogoLeccionesViewModel
import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import com.ufps.signaco.feature.progreso.domain.usecase.ObtenerResumenDeProgresoUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CatalogoLeccionesViewModelTest {

    @get:Rule
    val reglaDeDispatcher = ReglaDeDispatcherPrincipal()

    private val leccion = leccionDePrueba(
        id = "vocales",
        senas = listOf(senaDePrueba("A", orden = 1), senaDePrueba("E", orden = 2)),
        categoria = Categoria.VOCALES,
    )

    private fun crearViewModel(
        lecciones: LeccionRepository = FakeLeccionRepository(listOf(leccion)),
        progreso: ProgresoRepository = FakeProgresoRepository(),
    ) = CatalogoLeccionesViewModel(
        ObtenerCatalogoDeLeccionesUseCase(
            leccionRepository = lecciones,
            progresoRepository = progreso,
            combinarProgreso = CombinarLeccionConProgresoUseCase(),
        ),
        ObtenerResumenDeProgresoUseCase(
            senaRepository = FakeSenaRepository(leccion.senas),
            progresoRepository = progreso,
            rachaRepository = FakeRachaRepository(),
            reloj = RelojFijo(dia = 100),
        ),
    )

    @Test
    fun `arranca en estado de carga`() = runTest {
        val viewModel = crearViewModel()

        assertTrue(viewModel.uiState.value.cargando)
    }

    @Test
    fun `publica el catalogo con el avance ya calculado`() = runTest {
        val viewModel = crearViewModel(
            progreso = FakeProgresoRepository(
                progresoInicial = listOf(ProgresoSena("A", intentos = 8, aciertos = 7)),
            ),
        )

        val estado = viewModel.uiState.first { !it.cargando }

        assertFalse(estado.hayError)
        assertEquals(1, estado.categorias.size)
        assertEquals(1, estado.categorias.single().lecciones.single().senasDominadas)
    }

    @Test
    fun `si el repositorio falla el estado lo dice en vez de quedarse cargando`() = runTest {
        val viewModel = crearViewModel(lecciones = LeccionRepositoryQueFalla())

        val estado = viewModel.uiState.first { !it.cargando }

        assertTrue(estado.hayError)
        assertTrue(estado.categorias.isEmpty())
    }

    @Test
    fun `reintentar vuelve a pedir el catalogo`() = runTest {
        val repositorio = FakeLeccionRepository(emptyList())
        val viewModel = crearViewModel(lecciones = repositorio)
        assertTrue(viewModel.uiState.first { !it.cargando }.categorias.isEmpty())

        repositorio.lecciones.value = listOf(leccion)
        viewModel.reintentar()

        val estado = viewModel.uiState.first { !it.cargando && it.categorias.isNotEmpty() }
        assertEquals(1, estado.categorias.size)
    }
}
