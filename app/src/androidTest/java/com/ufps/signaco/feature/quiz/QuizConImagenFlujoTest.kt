package com.ufps.signaco.feature.quiz

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ufps.signaco.R
import com.ufps.signaco.core.comun.RelojDelSistema
import com.ufps.signaco.core.database.CatalogoInicial
import com.ufps.signaco.core.database.SignaCoDatabase
import com.ufps.signaco.core.database.baseConCatalogo
import com.ufps.signaco.core.database.entity.RachaEntity
import com.ufps.signaco.core.senas.data.ReferenciasEnRecursos
import com.ufps.signaco.core.senas.data.SenaRepositoryRoom
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.lecciones.data.LeccionRepositoryRoom
import com.ufps.signaco.feature.progreso.data.RachaRepositoryRoom
import com.ufps.signaco.feature.progreso.domain.usecase.ActualizarRachaUseCase
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz
import com.ufps.signaco.feature.quiz.domain.usecase.CalificarQuizUseCase
import com.ufps.signaco.feature.quiz.domain.usecase.GenerarQuizUseCase
import com.ufps.signaco.feature.quiz.presentation.QuizScreen
import com.ufps.signaco.feature.quiz.presentation.QuizViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * El quiz con imagen de punta a punta: ViewModel real, repositorios reales y Room en memoria,
 * armados a mano (sin Hilt de pruebas). Se contesta todo bien y se comprueba que termina, que
 * cuenta para la racha y que no aprueba la lección ni registra intentos: eso es de la parte con
 * cámara.
 */
@RunWith(AndroidJUnit4::class)
class QuizConImagenFlujoTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var base: SignaCoDatabase
    private lateinit var viewModel: QuizViewModel
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun armar() {
        base = baseConCatalogo()
        val referencias = ReferenciasEnRecursos()
        viewModel = QuizViewModel(
            savedStateHandle = SavedStateHandle(mapOf("leccionId" to CatalogoInicial.LECCION_VOCALES)),
            generarQuiz = GenerarQuizUseCase(
                leccionRepository = LeccionRepositoryRoom(base.leccionDao(), referencias),
                senaRepository = SenaRepositoryRoom(base.senaDao(), referencias),
            ),
            calificarQuiz = CalificarQuizUseCase(),
            actualizarRacha = ActualizarRachaUseCase(RachaRepositoryRoom(base.rachaDao()), RelojDelSistema()),
        )
    }

    @After
    fun cerrar() = base.close()

    @Test
    fun contestarTodoBienTerminaElQuizConTodosLosAciertos() {
        var resultado: ResultadoQuiz? = null
        compose.setContent {
            SignaCoTheme {
                QuizScreen(
                    onVolver = {},
                    onQuizTerminado = { _, r -> resultado = r },
                    viewModel = viewModel,
                )
            }
        }

        compose.waitUntil(TIEMPO_MS) { viewModel.uiState.value.preguntas.isNotEmpty() }
        val total = viewModel.uiState.value.total
        assertEquals(4, total) // Vocales: A, I, O y U, todas con imagen.

        repeat(total) {
            val estado = viewModel.uiState.value
            compose.onNodeWithText(estado.preguntaActual!!.letraCorrecta).performClick()
            val boton = if (estado.esLaUltima) R.string.quiz_terminar else R.string.quiz_siguiente
            compose.onNodeWithText(contexto.getString(boton)).performClick()
            compose.waitForIdle()
        }

        compose.waitUntil(TIEMPO_MS) { resultado != null }
        assertEquals(total, resultado!!.aciertos)
        assertEquals(total, resultado!!.total)

        runBlocking {
            assertNotNull("cuenta como actividad del día", base.rachaDao().obtenerRacha(RachaEntity.FILA_UNICA))
            assertTrue("no aprueba la lección", base.leccionDao().observarLeccionesCompletadas().first().isEmpty())
            assertTrue("no registra intentos de seña", base.progresoDao().observarProgreso().first().isEmpty())
        }
    }

    private companion object {
        const val TIEMPO_MS = 5_000L
    }
}
