package com.ufps.signaco.feature.practica.presentation.camara

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.componentes.BotonPrincipal
import com.ufps.signaco.core.ui.componentes.BotonTonal
import com.ufps.signaco.core.ui.componentes.CuentaRegresiva
import com.ufps.signaco.core.ui.componentes.EstiloDeMano
import com.ufps.signaco.core.ui.componentes.MedidorDeCoincidencia
import com.ufps.signaco.core.ui.componentes.PildoraDeEstado
import com.ufps.signaco.core.ui.componentes.TonoDePildora
import com.ufps.signaco.core.ui.componentes.marcoDeCamara
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.signaCoColores
import com.ufps.signaco.feature.practica.domain.model.EstadoDeReconocimiento
import kotlin.math.roundToInt

/*
 * Piezas de la cámara que comparten la práctica y el quiz con cámara (RF15). Viven aquí y no en
 * cada pantalla para que las dos se vean y se comporten igual.
 */

/**
 * Lo que el visor necesita para dibujarse. Cada pantalla lo arma con su propio estado.
 *
 * @param noReconocida el modelo se estabilizó en «no es una seña»: la píldora lo dice.
 * @param cuentaRegresiva 3, 2, 1 antes del intento, 0 al empezar, null si no hay cuenta.
 */
data class EstadoDelVisor(
    val puntosDeMano: FloatArray = FloatArray(0),
    val proporcionDeImagen: Float = 0f,
    val capturando: Boolean = false,
    val estadoDeReconocimiento: EstadoDeReconocimiento = EstadoDeReconocimiento.SIN_DETECCION,
    val noReconocida: Boolean = false,
    val coincidencia: Float = 0f,
    val umbral: Float = 0f,
    val cuentaRegresiva: Int? = null,
)

/** La hoja de resultado solo sale con un intento confirmado; mientras tanto la cámara manda. */
enum class ResultadoDelIntento { NINGUNO, ACIERTO, FALLO }

/** RF10: un control único para iniciar y detener. Detener es tonal: no compite con la cámara. */
@Composable
fun ControlDeCaptura(
    capturando: Boolean,
    contando: Boolean,
    onAlternarCaptura: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (contando) {
        // Durante la cuenta regresiva el mismo control la cancela, por si la persona no está lista.
        BotonTonal(
            texto = stringResource(R.string.practica_cancelar_cuenta),
            onClick = onAlternarCaptura,
            icono = R.drawable.ic_close,
            modifier = modifier.fillMaxWidth(),
        )
    } else if (capturando) {
        BotonTonal(
            texto = stringResource(R.string.practica_detener),
            onClick = onAlternarCaptura,
            icono = R.drawable.ic_stop,
            modifier = modifier.fillMaxWidth(),
        )
    } else {
        BotonPrincipal(
            texto = stringResource(R.string.practica_iniciar_intento),
            onClick = onAlternarCaptura,
            icono = R.drawable.ic_circle,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

/**
 * La vista de la cámara con todo lo que se dibuja encima: los puntos de la mano, el marco, la
 * píldora de estado (RF09), el panel de coincidencia y la cuenta regresiva. La usan la práctica
 * y el quiz con cámara.
 */
@Composable
fun VisorDeCamara(
    estado: EstadoDelVisor,
    resultado: ResultadoDelIntento,
    vistaDeCamara: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = MaterialTheme.signaCoColores
    val papel = MaterialTheme.colorScheme.surface
    // Amarillo en los puntos solo mientras se reconoce; en el fallo vuelven a neutro.
    val estiloDeMano = when (resultado) {
        ResultadoDelIntento.ACIERTO -> EstiloDeMano(linea = papel, relleno = papel, contorno = MaterialTheme.colorScheme.primary)
        ResultadoDelIntento.FALLO -> EstiloDeMano(linea = papel, relleno = colores.borde, contorno = papel)
        ResultadoDelIntento.NINGUNO -> EstiloDeMano(linea = papel, relleno = colores.resaltador, contorno = colores.camaraBaja)
    }

    Box(
        modifier = modifier
            .clip(Forma.contenedorGrande)
            .background(Brush.verticalGradient(listOf(colores.camaraAlta, colores.camaraBaja))),
    ) {
        vistaDeCamara()
        CapaDeMano(
            puntos = estado.puntosDeMano,
            proporcionDeImagen = estado.proporcionDeImagen,
            estilo = estiloDeMano,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .padding(start = Espacio.lg, end = Espacio.lg, top = Espacio.xxl + Espacio.md, bottom = Espacio.lg)
                .marcoDeCamara(colores.cuadricula),
        )
        // Píldora arriba y panel abajo en una columna: con la fuente grande el visor se achica
        // y así nunca se montan uno sobre otro.
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(Espacio.md),
        ) {
            Pildora(estado, resultado)
            if (resultado == ResultadoDelIntento.NINGUNO && estado.estadoDeReconocimiento == EstadoDeReconocimiento.RECONOCIENDO) {
                PanelDeCoincidencia(coincidencia = estado.coincidencia, umbral = estado.umbral)
            }
        }
        estado.cuentaRegresiva?.let { segundos ->
            CuentaRegresiva(segundos = segundos, modifier = Modifier.align(Alignment.Center))
        }
    }
}

/** RF09: el estado del reconocimiento se ve siempre, con texto además del color. */
@Composable
private fun Pildora(estado: EstadoDelVisor, resultado: ResultadoDelIntento, modifier: Modifier = Modifier) {
    when {
        resultado == ResultadoDelIntento.ACIERTO -> PildoraDeEstado(
            texto = stringResource(R.string.practica_sena_confirmada),
            tono = TonoDePildora.CONFIRMADA,
            icono = R.drawable.ic_check,
            modifier = modifier,
        )
        resultado == ResultadoDelIntento.FALLO ||
            estado.noReconocida -> PildoraDeEstado(
            texto = stringResource(R.string.practica_sin_coincidencia),
            tono = TonoDePildora.NEUTRA,
            icono = R.drawable.ic_close,
            modifier = modifier,
        )
        !estado.capturando -> PildoraDeEstado(
            texto = stringResource(R.string.practica_camara_lista),
            tono = TonoDePildora.NEUTRA,
            modifier = modifier,
        )
        estado.estadoDeReconocimiento == EstadoDeReconocimiento.RECONOCIENDO -> PildoraDeEstado(
            texto = stringResource(R.string.practica_estado_reconociendo),
            tono = TonoDePildora.ACTIVA,
            modifier = modifier,
        )
        else -> PildoraDeEstado(
            texto = stringResource(R.string.practica_estado_buscando_manos),
            tono = TonoDePildora.ACTIVA,
            modifier = modifier,
        )
    }
}

@Composable
private fun PanelDeCoincidencia(coincidencia: Float, umbral: Float, modifier: Modifier = Modifier) {
    val papel = MaterialTheme.colorScheme.surface
    val porcentaje = (coincidencia * 100).roundToInt()
    val umbralEnPorcentaje = (umbral * 100).roundToInt()
    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.xs),
        modifier = modifier
            .fillMaxWidth()
            .clip(Forma.contenedor)
            .background(MaterialTheme.signaCoColores.panelCamara.copy(alpha = OPACIDAD_DEL_PANEL))
            .padding(Espacio.md),
    ) {
        Row {
            Text(
                text = stringResource(R.string.practica_coincidencia),
                style = MaterialTheme.typography.titleMedium,
                color = papel,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.practica_porcentaje, porcentaje),
                style = MaterialTheme.typography.titleMedium,
                color = papel,
            )
        }
        MedidorDeCoincidencia(
            coincidencia = coincidencia,
            umbral = umbral,
            descripcionAccesible = stringResource(R.string.cd_coincidencia, porcentaje, umbralEnPorcentaje),
            pista = papel.copy(alpha = OPACIDAD_DE_LA_PISTA),
            relleno = papel,
            marca = papel,
        )
        Text(
            text = stringResource(R.string.practica_umbral, umbralEnPorcentaje),
            style = MaterialTheme.typography.bodyLarge,
            color = papel,
        )
    }
}

fun Context.abrirAjustesDeLaApp() {
    runCatching {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

/**
 * Tiempo que el sistema recomienda dejar un texto en pantalla, partiendo de [base]. Devuelve
 * más si la persona tiene subido el ajuste de accesibilidad "tiempo para realizar una acción".
 * Por debajo de Android 10 esa API no existe y se usa [base] tal cual.
 */
fun Context.esperaRecomendadaParaLeer(base: Long): Long {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return base
    val gestor = getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        ?: return base
    return gestor.getRecommendedTimeoutMillis(
        base.toInt(),
        AccessibilityManager.FLAG_CONTENT_TEXT,
    ).toLong()
}

private const val OPACIDAD_DEL_PANEL = 0.85f
private const val OPACIDAD_DE_LA_PISTA = 0.25f
