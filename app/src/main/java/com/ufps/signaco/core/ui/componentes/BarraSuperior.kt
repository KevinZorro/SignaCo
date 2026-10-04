package com.ufps.signaco.core.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.theme.SignaCoTheme

/** Cerrar se usa en los flujos que se abandonan (práctica, quiz); volver, en la navegación normal. */
enum class Navegacion { CERRAR, VOLVER }

/**
 * Barra superior con el título centrado. El atrás del sistema sigue funcionando: este botón es
 * un atajo visible, no el único camino.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    navegacion: Navegacion,
    onNavegar: () -> Unit,
    modifier: Modifier = Modifier,
    titulo: String? = null,
    acciones: @Composable () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            if (titulo != null) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavegar) {
                Icon(
                    painter = painterResource(
                        if (navegacion == Navegacion.CERRAR) R.drawable.ic_close else R.drawable.ic_arrow_back,
                    ),
                    contentDescription = stringResource(
                        if (navegacion == Navegacion.CERRAR) R.string.comun_cerrar else R.string.comun_volver,
                    ),
                )
            }
        },
        actions = { acciones() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BarraSuperiorPreview() {
    SignaCoTheme {
        Column {
            BarraSuperior(navegacion = Navegacion.VOLVER, onNavegar = {})
            BarraSuperior(
                navegacion = Navegacion.CERRAR,
                onNavegar = {},
                titulo = "Quiz de consonantes B, D, F y G con un nombre muy largo",
                acciones = {
                    IconButton(onClick = {}) {
                        Icon(painterResource(R.drawable.ic_help), contentDescription = null)
                    }
                },
            )
        }
    }
}
