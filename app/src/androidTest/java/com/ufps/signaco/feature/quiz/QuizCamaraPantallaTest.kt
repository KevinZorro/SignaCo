package com.ufps.signaco.feature.quiz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.practica.presentation.EstadoDelPermiso
import com.ufps.signaco.feature.quiz.domain.model.PreguntaDeCamara
import com.ufps.signaco.feature.quiz.presentation.EstadoDePregunta
import com.ufps.signaco.feature.quiz.presentation.QuizCamaraContenido
import com.ufps.signaco.feature.quiz.presentation.QuizCamaraUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** La pantalla del quiz con cámara (RF15), con estados armados a mano y sin cámara. */
@RunWith(AndroidJUnit4::class)
class QuizCamaraPantallaTest {

    @get:Rule
    val compose = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun muestraSoloLaLetraSinLaImagenDeReferencia() {
        mostrar(estado())

        compose.onNodeWithText(contexto.getString(R.string.quiz_camara_letra, "B")).assertIsDisplayed()
        compose.onAllNodesWithContentDescription(contexto.getString(R.string.cd_imagen_de_sena, "B"))
            .fetchSemanticsNodes()
            .let { assertEquals(0, it.size) }
        compose.onNodeWithText(contexto.getString(R.string.quiz_camara_titulo, 1, 5)).assertIsDisplayed()
    }

    @Test
    fun saltarLaPreguntaAvisaAlViewModel() {
        var saltos = 0
        mostrar(estado(), onSaltar = { saltos++ })

        compose.onNodeWithText(contexto.getString(R.string.quiz_camara_saltar)).performClick()
        assertEquals(1, saltos)
    }

    @Test
    fun trasUnFalloDiceCuantosIntentosQuedanYPermiteIntentarYa() {
        var intentos = 0
        mostrar(
            estado(EstadoDePregunta.OtraOportunidad(letraVista = "D", intentosRestantes = 2)),
            onIntentarDeNuevo = { intentos++ },
        )

        compose.onNodeWithText(contexto.getString(R.string.practica_estado_otra_letra, "D", "B")).assertIsDisplayed()
        compose.onNodeWithText(contexto.resources.getQuantityString(R.plurals.quiz_camara_quedan, 2, 2))
            .assertIsDisplayed()
        compose.onNodeWithText(contexto.getString(R.string.quiz_camara_intentar_ya)).performClick()
        assertEquals(1, intentos)
    }

    @Test
    fun laUltimaPreguntaPerdidaLlevaAlResultado() {
        var siguientes = 0
        mostrar(estado(EstadoDePregunta.Fallada(letraVista = null), indice = 4), onSiguiente = { siguientes++ })

        compose.onNodeWithText(contexto.getString(R.string.quiz_camara_ver_resultado)).performClick()
        assertEquals(1, siguientes)
    }

    @Test
    fun acertarOfreceLaSiguientePregunta() {
        mostrar(estado(EstadoDePregunta.Acertada(confianza = 0.9f)))

        compose.onNodeWithText(contexto.getString(R.string.quiz_camara_siguiente)).assertIsDisplayed()
    }

    private fun estado(
        estadoDePregunta: EstadoDePregunta = EstadoDePregunta.Respondiendo,
        indice: Int = 0,
    ) = QuizCamaraUiState(
        cargando = false,
        preguntas = listOf("B", "D", "F", "B", "G").map { PreguntaDeCamara(it, it) },
        indiceActual = indice,
        intentosPorPregunta = 3,
        estadoDePregunta = estadoDePregunta,
        permiso = EstadoDelPermiso.CONCEDIDO,
        capturando = true,
        reducirAnimaciones = true,
        retroalimentacionHaptica = false,
    )

    private fun mostrar(
        estado: QuizCamaraUiState,
        onSaltar: () -> Unit = {},
        onIntentarDeNuevo: () -> Unit = {},
        onSiguiente: () -> Unit = {},
    ) {
        compose.setContent {
            SignaCoTheme {
                QuizCamaraContenido(
                    uiState = estado,
                    onVolver = {},
                    onPermitirCamara = {},
                    onAbrirAjustes = {},
                    onAlternarCaptura = {},
                    onCambiarCamara = {},
                    onSaltar = onSaltar,
                    onIntentarDeNuevo = onIntentarDeNuevo,
                    onSiguiente = onSiguiente,
                    onReintentarCarga = {},
                    onReintentarTrasError = {},
                )
            }
        }
    }
}
