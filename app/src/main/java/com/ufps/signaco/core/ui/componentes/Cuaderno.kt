package com.ufps.signaco.core.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.core.ui.theme.signaCoColores

/**
 * Cuadrícula de cuaderno. Reservada al cuaderno de progreso y a la tarjeta de la palabra:
 * en el resto de la app el fondo es liso.
 */
fun Modifier.cuadriculado(color: Color, lado: Dp = Medida.ladoDeCuadricula): Modifier =
    drawBehind {
        val paso = lado.toPx()
        val grosor = Medida.trazoFino.toPx()
        var x = paso
        while (x < size.width) {
            drawLine(color, Offset(x, 0f), Offset(x, size.height), grosor)
            x += paso
        }
        var y = paso
        while (y < size.height) {
            drawLine(color, Offset(0f, y), Offset(size.width, y), grosor)
            y += paso
        }
    }

/** La seña que se practica, escrita en el cuaderno. Sin borde exterior. Sin [indicacion], solo la palabra. */
@Composable
fun TarjetaDePalabra(
    palabra: String,
    indicacion: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.xxs),
        modifier = modifier
            .fillMaxWidth()
            .clip(Forma.contenedor)
            .background(MaterialTheme.colorScheme.surface)
            .cuadriculado(MaterialTheme.signaCoColores.cuadricula)
            .padding(horizontal = Espacio.lg, vertical = Espacio.md),
    ) {
        Text(
            text = palabra,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() },
        )
        if (indicacion != null) {
            Text(
                text = indicacion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TarjetaDePalabraPreview() {
    SignaCoTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.md),
            modifier = Modifier.padding(Espacio.margen),
        ) {
            TarjetaDePalabra(palabra = "A", indicacion = "Haz esta seña frente a la cámara")
            TarjetaDePalabra(palabra = "Letra W", indicacion = "Haz esta seña frente a la cámara")
        }
    }
}
