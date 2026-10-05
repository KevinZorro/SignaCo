package com.ufps.signaco.feature.diccionario.presentation.teoria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.core.ui.componentes.BarraSuperior
import com.ufps.signaco.core.ui.componentes.BotonPrincipal
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.componentes.Navegacion
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.EstiloNumero
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.SignaCoTheme

@Composable
fun TeoriaSenaScreen(
    onCerrar: () -> Unit,
    onIniciarIntento: (leccionId: String, senaId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeoriaSenaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TeoriaSenaContenido(
        uiState = uiState,
        onCerrar = onCerrar,
        onIniciarIntento = { leccionId -> onIniciarIntento(leccionId, viewModel.senaId) },
        onReintentar = viewModel::reintentar,
        modifier = modifier,
    )
}

@Composable
fun TeoriaSenaContenido(
    uiState: TeoriaSenaUiState,
    onCerrar: () -> Unit,
    onIniciarIntento: (String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sena = uiState.sena
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                navegacion = Navegacion.CERRAR,
                onNavegar = onCerrar,
                titulo = stringResource(R.string.teoria_titulo),
            )
        },
    ) { relleno ->
        when {
            uiState.cargando -> EstadoDeCarga(Modifier.padding(relleno))

            uiState.hayError -> EstadoDeError(
                mensaje = stringResource(R.string.comun_error_generico),
                onReintentar = onReintentar,
                modifier = Modifier.padding(relleno),
            )

            sena == null -> MensajeVacio(
                mensaje = stringResource(R.string.diccionario_sin_resultados),
                modifier = Modifier.padding(relleno),
            )

            else -> Column(
                verticalArrangement = Arrangement.spacedBy(Espacio.md),
                modifier = Modifier
                    .padding(relleno)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Espacio.margen, vertical = Espacio.md),
            ) {
                Text(
                    text = stringResource(R.string.teoria_consejos),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TarjetaDeTeoria(sena)
                val leccionId = uiState.leccionId
                BotonPrincipal(
                    texto = stringResource(R.string.teoria_iniciar),
                    onClick = { if (leccionId != null) onIniciarIntento(leccionId) },
                    icono = R.drawable.ic_circle,
                    habilitado = leccionId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Espacio.xs),
                )
                if (leccionId == null) {
                    Text(
                        text = stringResource(R.string.teoria_no_practicable),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Superficie tonal lisa, sin cuadrícula ni borde: la cuadrícula es del cuaderno. */
@Composable
private fun TarjetaDeTeoria(sena: Sena) {
    val descripcionDeLetra = stringResource(R.string.cd_letra_grande, sena.letra)
    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.xs),
        modifier = Modifier
            .fillMaxWidth()
            .clip(Forma.contenedorGrande)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(Espacio.lg),
    ) {
        Text(
            text = sena.nombre,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = sena.descripcion,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = sena.letra,
            style = EstiloNumero,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = Espacio.lg)
                .semantics { contentDescription = descripcionDeLetra },
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TeoriaSenaPreview() {
    SignaCoTheme {
        TeoriaSenaContenido(
            uiState = TeoriaSenaUiState(
                cargando = false,
                sena = Sena(
                    "A", "A", "Letra A",
                    "Puño cerrado con el pulgar extendido pegado al costado del índice.",
                    Categoria.VOCALES, 1, true,
                ),
                leccionId = "leccion_vocales",
            ),
            onCerrar = {},
            onIniciarIntento = {},
            onReintentar = {},
        )
    }
}
