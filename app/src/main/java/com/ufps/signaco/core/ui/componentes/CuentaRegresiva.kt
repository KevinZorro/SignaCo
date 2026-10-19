package com.ufps.signaco.core.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.SignaCoTheme

/**
 * La cuenta regresiva sobre la cámara antes de un intento: «Prepárate» y 3, 2, 1; en 0, «¡Ya!».
 *
 * Es una región viva: TalkBack anuncia cada número y el inicio del intento sin que la persona
 * tenga que mover el foco (RNF-10). El número es grande y va sobre una superficie lisa, así que
 * se lee aunque el fondo de la cámara cambie.
 *
 * @param segundos lo que falta (3, 2, 1) o 0 cuando el intento acaba de empezar.
 */
@Composable
fun CuentaRegresiva(segundos: Int, modifier: Modifier = Modifier) {
    val anuncio = if (segundos > 0) {
        stringResource(R.string.cuenta_regresiva_anuncio, segundos)
    } else {
        stringResource(R.string.cuenta_regresiva_inicio_anuncio)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(TAMANO)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clearAndSetSemantics {
                contentDescription = anuncio
                liveRegion = LiveRegionMode.Assertive
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Espacio.xxs),
        ) {
            if (segundos > 0) {
                Text(
                    text = stringResource(R.string.cuenta_regresiva_preparate),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = segundos.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    text = stringResource(R.string.cuenta_regresiva_inicio),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private val TAMANO = 144.dp

@Preview(showBackground = true, backgroundColor = 0xFF1B2459)
@Composable
private fun CuentaRegresivaPreview() {
    SignaCoTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Espacio.md),
            modifier = Modifier.padding(Espacio.md),
        ) {
            CuentaRegresiva(segundos = 3)
            CuentaRegresiva(segundos = 0)
        }
    }
}
