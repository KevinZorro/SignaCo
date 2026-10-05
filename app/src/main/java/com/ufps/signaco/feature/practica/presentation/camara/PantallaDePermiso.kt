package com.ufps.signaco.feature.practica.presentation.camara

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.componentes.BotonDeTexto
import com.ufps.signaco.core.ui.componentes.BotonPrincipal
import com.ufps.signaco.core.ui.componentes.EsquemaDeMano
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.SignaCoTheme

/**
 * Explica para qué se usa la cámara antes de pedir el permiso (01). Con [denegado] la acción
 * secundaria lleva a los ajustes de la app, porque el sistema puede haber dejado de mostrar el
 * diálogo.
 */
@Composable
fun PantallaDePermiso(
    denegado: Boolean,
    onPermitir: () -> Unit,
    onAhoraNo: () -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.lg),
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Espacio.margen, vertical = Espacio.lg),
        ) {
            Box {
                EsquemaDeMano(descripcion = stringResource(R.string.cd_esquema_puntos), aLaDerecha = true)
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.practica_permiso_puntos))
                        }
                        append(" ")
                        append(stringResource(R.string.practica_permiso_puntos_detalle))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.45f)
                        .padding(Espacio.md),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(Espacio.sm)) {
                Text(
                    text = stringResource(R.string.practica_permiso_titulo),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.practica_permiso_explicacion),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(Espacio.md)) {
                Garantia(R.drawable.ic_smartphone, R.string.practica_permiso_telefono, R.string.practica_permiso_telefono_detalle)
                Garantia(R.drawable.ic_videocam_off, R.string.practica_permiso_video, R.string.practica_permiso_video_detalle)
                Garantia(R.drawable.ic_wifi_off, R.string.practica_permiso_conexion, R.string.practica_permiso_conexion_detalle)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BotonPrincipal(
                    texto = stringResource(R.string.practica_permiso_conceder),
                    onClick = onPermitir,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (denegado) {
                    BotonDeTexto(
                        texto = stringResource(R.string.practica_permiso_ajustes),
                        onClick = onAbrirAjustes,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    BotonDeTexto(
                        texto = stringResource(R.string.practica_permiso_ahora_no),
                        onClick = onAhoraNo,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    text = stringResource(R.string.practica_permiso_nota),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Espacio.xs),
                )
            }
        }
    }
}

@Composable
private fun Garantia(@DrawableRes icono: Int, titulo: Int, detalle: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(Espacio.md)) {
        Icon(
            painter = painterResource(icono),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(Espacio.xxs)) {
            Text(
                text = stringResource(titulo),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(detalle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun PantallaDePermisoPreview() {
    SignaCoTheme {
        PantallaDePermiso(denegado = false, onPermitir = {}, onAhoraNo = {}, onAbrirAjustes = {})
    }
}
