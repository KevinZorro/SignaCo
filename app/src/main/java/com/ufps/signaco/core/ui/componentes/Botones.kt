package com.ufps.signaco.core.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme

/** Acción principal de la pantalla. Solo una por pantalla. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icono: Int? = null,
    habilitado: Boolean = true,
    cargando: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = Forma.pildora,
        modifier = modifier.heightIn(min = Medida.alturaDeBoton),
    ) {
        ContenidoDeBoton(texto = texto, icono = icono, cargando = cargando)
    }
}

/** Acción secundaria que no debe competir con la principal (p. ej. «Detener»). */
@Composable
fun BotonTonal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icono: Int? = null,
    habilitado: Boolean = true,
    cargando: Boolean = false,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = Forma.pildora,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier.heightIn(min = Medida.alturaDeBoton),
    ) {
        ContenidoDeBoton(texto = texto, icono = icono, cargando = cargando)
    }
}

/** Acción discreta. [color] permite bajarla a Tinta suave cuando no debe llamar la atención. */
@Composable
fun BotonDeTexto(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icono: Int? = null,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    TextButton(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = Forma.pildora,
        colors = ButtonDefaults.textButtonColors(contentColor = color),
        modifier = modifier.heightIn(min = Medida.areaTactil),
    ) {
        ContenidoDeBoton(texto = texto, icono = icono, cargando = cargando)
    }
}

@Composable
private fun ContenidoDeBoton(texto: String, @DrawableRes icono: Int?, cargando: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = Espacio.xs),
    ) {
        if (cargando) {
            CircularProgressIndicator(
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
                modifier = Modifier.size(Medida.icono),
            )
            Spacer(Modifier.width(Espacio.sm))
        } else if (icono != null) {
            Icon(
                painter = painterResource(icono),
                // El texto del botón ya dice la acción.
                contentDescription = null,
                modifier = Modifier.size(Medida.icono),
            )
            Spacer(Modifier.width(Espacio.sm))
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BotonesPreview() {
    SignaCoTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.md),
            modifier = Modifier.padding(Espacio.margen),
        ) {
            BotonPrincipal("Empezar práctica", {}, Modifier.fillMaxWidth())
            BotonPrincipal("Iniciar intento", {}, Modifier.fillMaxWidth(), icono = R.drawable.ic_circle)
            BotonPrincipal("Guardar cambios", {}, Modifier.fillMaxWidth(), cargando = true)
            BotonPrincipal("Pruébalo con la cámara", {}, Modifier.fillMaxWidth(), habilitado = false)
            BotonTonal("Detener", {}, Modifier.fillMaxWidth(), icono = R.drawable.ic_stop)
            BotonDeTexto("Ver mi progreso", {}, Modifier.fillMaxWidth())
            BotonDeTexto(
                texto = "Borrar mi progreso",
                onClick = {},
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
