package com.ufps.signaco.core.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.core.ui.theme.signaCoColores

/** Recuadro punteado donde debe quedar la mano. Va en Cuadrícula: no compite con la cámara. */
fun Modifier.marcoDeCamara(color: Color): Modifier = drawWithContent {
    drawContent()
    val grosor = 2.dp.toPx()
    val radio = 24.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(grosor / 2, grosor / 2),
        size = size.copy(width = size.width - grosor, height = size.height - grosor),
        cornerRadius = CornerRadius(radio, radio),
        style = Stroke(
            width = grosor,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 8.dp.toPx())),
        ),
    )
}

/**
 * NEUTRA: cámara lista o sin coincidencia. ACTIVA: buscando o reconociendo (el indicador
 * amarillo marca que se está aprendiendo ahora). CONFIRMADA: la seña se aceptó.
 */
enum class TonoDePildora { NEUTRA, ACTIVA, CONFIRMADA }

/**
 * Estado del reconocimiento sobre la cámara (RF09). Es una región viva: TalkBack anuncia cada
 * cambio sin que la persona tenga que buscarlo.
 */
@Composable
fun PildoraDeEstado(
    texto: String,
    tono: TonoDePildora,
    modifier: Modifier = Modifier,
    icono: Int? = null,
) {
    val colores = MaterialTheme.signaCoColores
    val fondo = if (tono == TonoDePildora.CONFIRMADA) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contenido = if (tono == TonoDePildora.CONFIRMADA) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.xs),
        modifier = modifier
            .clip(Forma.pildora)
            .background(fondo)
            .padding(horizontal = Espacio.md, vertical = Espacio.xs)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        when {
            icono != null -> Icon(
                painter = painterResource(icono),
                contentDescription = null,
                tint = contenido,
                modifier = Modifier.size(Medida.icono),
            )
            else -> Box(
                Modifier
                    .size(Espacio.sm)
                    .clip(Forma.pildora)
                    .background(if (tono == TonoDePildora.ACTIVA) colores.resaltador else colores.borde)
                    .border(Medida.trazoFino, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), Forma.pildora),
            )
        }
        Text(text = texto, style = MaterialTheme.typography.titleMedium, color = contenido)
    }
}

/**
 * Coincidencia con la seña objetivo y la marca del umbral que viene de config_rechazo.json.
 *
 * @param coincidencia y [umbral] entre 0f y 1f.
 * @param relleno color del tramo alcanzado: claro sobre la cámara, Corrección en el error.
 */
@Composable
fun MedidorDeCoincidencia(
    coincidencia: Float,
    umbral: Float,
    descripcionAccesible: String,
    pista: Color,
    relleno: Color,
    marca: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(Espacio.md)
            .clearAndSetSemantics { contentDescription = descripcionAccesible },
    ) {
        val alto = 8.dp.toPx()
        val arriba = (size.height - alto) / 2
        val radio = CornerRadius(alto / 2, alto / 2)
        drawRoundRect(pista, Offset(0f, arriba), size.copy(height = alto), radio)
        val avance = size.width * coincidencia.coerceIn(0f, 1f)
        if (avance > 0f) {
            drawRoundRect(relleno, Offset(0f, arriba), size.copy(width = avance, height = alto), radio)
        }
        val x = size.width * umbral.coerceIn(0f, 1f)
        drawLine(marca, Offset(x, 0f), Offset(x, size.height), 2.dp.toPx(), StrokeCap.Round)
    }
}

@Preview(widthDp = 360)
@Composable
private fun CamaraPreview() {
    SignaCoTheme {
        val colores = MaterialTheme.signaCoColores
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.md),
            modifier = Modifier
                .background(colores.camaraAlta)
                .padding(Espacio.margen),
        ) {
            PildoraDeEstado("Cámara lista", TonoDePildora.NEUTRA)
            PildoraDeEstado("Buscando tu mano", TonoDePildora.ACTIVA)
            PildoraDeEstado("Seña confirmada", TonoDePildora.CONFIRMADA, icono = R.drawable.ic_check)
            PildoraDeEstado("Sin coincidencia", TonoDePildora.NEUTRA, icono = R.drawable.ic_close)
            MedidorDeCoincidencia(
                coincidencia = 0.68f,
                umbral = 0.1f,
                descripcionAccesible = "",
                pista = Color.White.copy(alpha = 0.25f),
                relleno = Color.White,
                marca = Color.White,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .marcoDeCamara(colores.cuadricula),
            )
        }
    }
}
