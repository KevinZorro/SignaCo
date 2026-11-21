package com.ufps.signaco.feature.lecciones

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ufps.signaco.R
import com.ufps.signaco.core.database.CatalogoInicial
import com.ufps.signaco.core.database.SignaCoDatabase
import com.ufps.signaco.core.database.baseConCatalogo
import com.ufps.signaco.core.senas.data.ReferenciasEnRecursos
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.lecciones.data.LeccionRepositoryRoom
import com.ufps.signaco.feature.lecciones.domain.usecase.CombinarLeccionConProgresoUseCase
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerDetalleDeLeccionUseCase
import com.ufps.signaco.feature.lecciones.presentation.detalle.DetalleLeccionScreen
import com.ufps.signaco.feature.lecciones.presentation.detalle.DetalleLeccionViewModel
import com.ufps.signaco.feature.progreso.data.ProgresoRepositoryRoom
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** La lección carga desde Room y «Empezar práctica» arranca por la indicación de la primera letra. */
@RunWith(AndroidJUnit4::class)
class DetalleLeccionPantallaTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var base: SignaCoDatabase
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun crear() {
        base = baseConCatalogo()
    }

    @After
    fun cerrar() = base.close()

    @Test
    fun cargaLasLetrasYEmpiezaPorLaPrimera() {
        val viewModel = DetalleLeccionViewModel(
            savedStateHandle = SavedStateHandle(mapOf("leccionId" to CatalogoInicial.LECCION_VOCALES)),
            obtenerDetalle = ObtenerDetalleDeLeccionUseCase(
                leccionRepository = LeccionRepositoryRoom(base.leccionDao(), ReferenciasEnRecursos()),
                progresoRepository = ProgresoRepositoryRoom(base.progresoDao(), base.leccionDao()),
                combinarProgreso = CombinarLeccionConProgresoUseCase(),
            ),
        )
        var practica: Pair<String, String>? = null
        compose.setContent {
            SignaCoTheme {
                DetalleLeccionScreen(
                    onVolver = {},
                    onSenaSeleccionada = { _, _ -> },
                    onPracticar = { leccion, sena -> practica = leccion to sena },
                    onHacerQuiz = {},
                    viewModel = viewModel,
                )
            }
        }

        compose.waitUntil(TIEMPO_MS) { viewModel.uiState.value.leccion != null }
        // La lista es un LazyColumn y el emulador de CI no tiene perfil de hardware: su pantalla
        // es de 320×640, la fila de la «U» queda bajo el pliegue y un LazyColumn no compone las
        // filas que quedan fuera. assertIsDisplayed() da «no mostrado» tanto para un nodo fuera
        // de pantalla como para uno que no existe, así que se llega a cada letra con scroll.
        listOf("A", "I", "O", "U").forEach { letra ->
            compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Letra $letra"))
            compose.onNodeWithText("Letra $letra").assertIsDisplayed()
        }
        compose.onNodeWithText(contexto.getString(R.string.detalle_empezar_practica)).performClick()
        assertEquals(CatalogoInicial.LECCION_VOCALES to "A", practica)
    }

    private companion object {
        const val TIEMPO_MS = 5_000L
    }
}
