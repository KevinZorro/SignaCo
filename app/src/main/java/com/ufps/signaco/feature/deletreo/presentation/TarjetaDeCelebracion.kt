package com.ufps.signaco.feature.deletreo.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.componentes.Sello
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Movimiento
import com.ufps.signaco.core.ui.theme.PanelCamara
import com.ufps.signaco.core.ui.theme.SignaCoTheme

/**
 * Lo que se ve al terminar una palabra, sobre la cámara.
 *
 * Es una tarjeta de papel con tinta (la «tarjeta de palabra» del sistema de diseño), no texto
 * suelto: sobre una imagen en vivo, que puede ser clara u oscura, el papel blanco con tinta
 * azul da el mismo contraste (más de 7:1) en cualquier fondo. Lleva el sello de acierto de la
 * app, que ya es el gesto de «bien hecho», y debajo lo que se deletreó, para que el logro se
 * vea. Un velo oscuro suave apaga la cámara detrás para que haya un solo foco. Sin sombras
 * (DESIGN.md): el borde fino separa la tarjeta cuando el fondo es blanco.
 *
 * Entra con un fundido y una escala corta, y sale con un fundido; con [reducirAnimaciones]
 * aparece y desaparece sin movimiento.
 */
@Composable
fun CapaDeCelebracion(
    visible: Boolean,
    frase: String,
    reducirAnimaciones: Boolean,
    modifier: Modifier = Modifier,
    /** Va entre el velo y la tarjeta (el confeti), para que nunca tape el texto. */
    intermedio: @Composable () -> Unit = {},
) {
    // Al terminar, el estado se limpia mientras la tarjeta aún se desvanece: se conserva la última
    // frase para que no se vacíe a mitad del fundido.
    var ultimaFrase by remember { mutableStateOf("") }
    if (frase.isNotEmpty()) ultimaFrase = frase
    AnimatedVisibility(
        visible = visible,
        enter = if (reducirAnimaciones) {
            EnterTransition.None
        } else {
            fadeIn(tween(Movimiento.medio)) +
                scaleIn(tween(Movimiento.largo, easing = LinearOutSlowInEasing), initialScale = 0.92f)
        },
        exit = if (reducirAnimaciones) ExitTransition.None else fadeOut(tween(Movimiento.corto)),
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(PanelCamara.copy(alpha = VELO)),
        ) {
            intermedio()
            TarjetaDeCelebracion(frase = ultimaFrase, animarSello = !reducirAnimaciones)
        }
    }
}

@Composable
fun TarjetaDeCelebracion(
    frase: String,
    modifier: Modifier = Modifier,
    animarSello: Boolean = true,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = Forma.contenedorGrande,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .padding(horizontal = Espacio.margen)
            .widthIn(max = 320.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Espacio.sm),
            modifier = Modifier.padding(horizontal = Espacio.lg, vertical = Espacio.lg),
        ) {
            Sello(animar = animarSello)
            Text(
                text = stringResource(R.string.deletreo_celebracion),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            if (frase.isNotEmpty()) {
                Text(
                    text = frase,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Opacidad del velo que apaga la cámara detrás de la tarjeta. */
private const val VELO = 0.55f

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun CelebracionPreview() {
    SignaCoTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CapaDeCelebracion(visible = true, frase = "MAMA PAPA", reducirAnimaciones = true)
        }
    }
}
