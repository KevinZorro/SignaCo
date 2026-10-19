package com.ufps.signaco.feature.practica

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.practica.domain.model.PasoTrasElAcierto
import com.ufps.signaco.feature.practica.presentation.EstadoDeRetroalimentacion
import com.ufps.signaco.feature.practica.presentation.EstadoDelPermiso
import com.ufps.signaco.feature.practica.presentation.PracticaContenido
import com.ufps.signaco.feature.practica.presentation.PracticaUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * La hoja que sale tras acertar ofrece el botón que corresponde a cómo se entró a practicar.
 * Se prueba el contenido sin ViewModel ni cámara: el estado se arma a mano.
 */
@RunWith(AndroidJUnit4::class)
class PracticaAciertoTest {

    @get:Rule
    val compose = createComposeRule()

    private fun texto(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test
    fun letraSueltaOfreceVolverALaLeccion() {
        var continuo = 0
        mostrarAcierto(PasoTrasElAcierto.VolverALaLeccion, soloEstaSena = true) { continuo++ }

        compose.onNodeWithText(texto(R.string.practica_volver_a_la_leccion)).assertIsDisplayed().performClick()
        compose.onNodeWithText(texto(R.string.practica_reintentar_sena)).assertIsDisplayed()
        assertEquals(1, continuo)
    }

    @Test
    fun leccionCompletaOfreceLaSiguienteSena() {
        mostrarAcierto(PasoTrasElAcierto.SiguienteSena("I"), soloEstaSena = false) {}

        compose.onNodeWithText(texto(R.string.practica_siguiente)).assertIsDisplayed()
    }

    @Test
    fun laUltimaLetraDeLaLeccionOfreceTerminar() {
        mostrarAcierto(PasoTrasElAcierto.TerminarLeccion, soloEstaSena = false) {}

        compose.onNodeWithText(texto(R.string.practica_terminar)).assertIsDisplayed()
    }

    private fun mostrarAcierto(
        paso: PasoTrasElAcierto,
        soloEstaSena: Boolean,
        onContinuar: () -> Unit,
    ) {
        val estado = PracticaUiState(
            cargando = false,
            senas = listOf("A", "I", "O", "U").mapIndexed { i, letra ->
                Sena(letra, letra, "Letra $letra", Categoria.VOCALES, i, true)
            },
            permiso = EstadoDelPermiso.CONCEDIDO,
            intentoConfirmado = true,
            retroalimentacion = EstadoDeRetroalimentacion.Acertada("A", 0.9f),
            reducirAnimaciones = true,
            retroalimentacionHaptica = false,
            soloEstaSena = soloEstaSena,
            pasoTrasElAcierto = paso,
        )
        compose.setContent {
            SignaCoTheme {
                PracticaContenido(
                    uiState = estado,
                    onVolver = {},
                    onPermitirCamara = {},
                    onAbrirAjustes = {},
                    onAlternarCaptura = {},
                    onCambiarCamara = {},
                    onReintentarSena = {},
                    onReintentar = {},
                    onContinuar = onContinuar,
                    onCambiarPistas = {},
                )
            }
        }
    }
}
