package com.ufps.signaco.feature.deletreo.presentation

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

private const val DURACION_MS = 1400
private const val PIEZAS = 60
private const val PAUSA_SIN_MOVIMIENTO_MS = 1400L

/**
 * Confeti que cae una vez y termina. Con [animar] en false, o si el usuario desactivó las animaciones
 * del sistema, no se dibuja y solo se espera lo mismo que duraría, para que la tarjeta alcance a leerse.
 */
@Composable
fun Confeti(alTerminar: () -> Unit, modifier: Modifier = Modifier, animar: Boolean = true) {
    val contexto = LocalContext.current
    val terminar = rememberUpdatedState(alTerminar)
    val colores = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
    )
    val piezas = remember { GeneradorDeConfeti.generar(PIEZAS, colores = 5) }
    val progreso = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val escala = Settings.Global.getFloat(contexto.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        if (animar && escala > 0f) {
            progreso.animateTo(1f, tween(DURACION_MS, easing = LinearOutSlowInEasing))
        } else {
            // Sin movimiento: la tarjeta se queda quieta el mismo rato para que se alcance a leer.
            delay(PAUSA_SIN_MOVIMIENTO_MS)
        }
        terminar.value()
    }

    if (!animar) return
    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progreso.value
        piezas.forEach { p ->
            val lado = p.tamano * size.width
            val centro = Offset(GeneradorDeConfeti.x(p, t) * size.width, GeneradorDeConfeti.y(p, t) * size.height)
            rotate(degrees = GeneradorDeConfeti.angulo(p, t), pivot = centro) {
                drawRect(
                    color = colores[p.color % colores.size],
                    topLeft = Offset(centro.x - lado / 2f, centro.y - lado),
                    size = Size(lado, lado * 2f),
                )
            }
        }
    }
}
