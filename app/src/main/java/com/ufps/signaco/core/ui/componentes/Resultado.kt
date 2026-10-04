package com.ufps.signaco.core.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Movimiento
import com.ufps.signaco.core.ui.theme.SignaCoTheme

/**
 * Hoja inferior del resultado de la práctica. Es la única superficie con sombra de la app:
 * se separa de la cámara que queda detrás.
 */
@Composable
fun HojaDeResultado(
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.md),
            modifier = Modifier.padding(
                start = Espacio.margen,
                end = Espacio.margen,
                top = Espacio.lg,
                bottom = Espacio.md,
            ),
            content = contenido,
        )
    }
}

/**
 * Sello «¡Bien!» del acierto. Entra con una sola animación corta, sin rebote; con
 * [animar] en false aparece quieto (ajuste «Reducir animaciones»). Es decorativo: el texto
 * que lo acompaña ya dice que se acertó.
 */
@Composable
fun Sello(
    modifier: Modifier = Modifier,
    animar: Boolean = true,
    lado: Dp = 112.dp,
) {
    val progreso = remember { Animatable(if (animar) 0f else 1f) }
    LaunchedEffect(animar) {
        if (animar) progreso.animateTo(1f, tween(Movimiento.largo, easing = LinearOutSlowInEasing))
    }
    val color = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(lado)
            .graphicsLayer {
                val escala = 0.85f + 0.15f * progreso.value
                scaleX = escala
                scaleY = escala
                alpha = progreso.value
            }
            .clearAndSetSemantics {},
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val grueso = 4.dp.toPx()
            drawCircle(color, radius = size.minDimension / 2 - grueso / 2, style = Stroke(grueso))
            drawCircle(color, radius = size.minDimension / 2 - 10.dp.toPx(), style = Stroke(1.5.dp.toPx()))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.rotate(-8f),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(lado / 3),
            )
            Text(
                text = stringResource(R.string.sello_bien),
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ResultadoPreview() {
    SignaCoTheme {
        HojaDeResultado {
            Sello(animar = false)
            BotonPrincipal("Siguiente seña", {}, Modifier.fillMaxWidth())
        }
    }
}
