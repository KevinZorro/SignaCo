package com.ufps.signaco.feature.practica

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.practica.presentation.EstadoDelPermiso
import com.ufps.signaco.feature.practica.presentation.PracticaContenido
import com.ufps.signaco.feature.practica.presentation.PracticaUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** La cuenta regresiva se ve sobre la cámara, se anuncia y se puede cancelar. */
@RunWith(AndroidJUnit4::class)
class PracticaCuentaRegresivaTest {

    @get:Rule
    val compose = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun muestraElNumeroYLoAnunciaATalkBack() {
        mostrar(cuentaRegresiva = 2) {}

        compose.onNodeWithContentDescription(contexto.getString(R.string.cuenta_regresiva_anuncio, 2))
            .assertIsDisplayed()
    }

    @Test
    fun duranteLaCuentaSeOfreceCancelarEnVezDeIniciar() {
        var toques = 0
        mostrar(cuentaRegresiva = 3) { toques++ }

        compose.onNodeWithText(contexto.getString(R.string.practica_cancelar_cuenta)).assertIsDisplayed().performClick()
        assertEquals(1, toques)
    }

    @Test
    fun alTerminarAnunciaQueElIntentoEmpezo() {
        mostrar(cuentaRegresiva = 0, capturando = true) {}

        compose.onNodeWithContentDescription(contexto.getString(R.string.cuenta_regresiva_inicio_anuncio))
            .assertIsDisplayed()
        compose.onNodeWithText(contexto.getString(R.string.practica_detener)).assertIsDisplayed()
    }

    @Test
    fun siSeCancelaQuedaIniciarIntentoDeRespaldo() {
        mostrar(cuentaRegresiva = null) {}

        compose.onNodeWithText(contexto.getString(R.string.practica_iniciar_intento)).assertIsDisplayed()
    }

    private fun mostrar(cuentaRegresiva: Int?, capturando: Boolean = false, onAlternarCaptura: () -> Unit) {
        val estado = PracticaUiState(
            cargando = false,
            senas = listOf(Sena("A", "A", "Letra A", Categoria.VOCALES, 1, true)),
            permiso = EstadoDelPermiso.CONCEDIDO,
            capturando = capturando,
            cuentaRegresiva = cuentaRegresiva,
            reducirAnimaciones = true,
        )
        compose.setContent {
            SignaCoTheme {
                PracticaContenido(
                    uiState = estado,
                    onVolver = {},
                    onPermitirCamara = {},
                    onAbrirAjustes = {},
                    onAlternarCaptura = onAlternarCaptura,
                    onCambiarCamara = {},
                    onReintentarSena = {},
                    onReintentar = {},
                    onContinuar = {},
                    onCambiarPistas = {},
                )
            }
        }
    }
}
