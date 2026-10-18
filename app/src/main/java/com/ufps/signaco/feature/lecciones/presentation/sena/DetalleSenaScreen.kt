package com.ufps.signaco.feature.lecciones.presentation.sena

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.ufps.signaco.core.ui.componentes.ReferenciaDeSena
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.EstiloNumero
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.SignaCoTheme

@Composable
fun DetalleSenaScreen(
    onCerrar: () -> Unit,
    onProbarConCamara: (leccionId: String, senaId: String, enRecorrido: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleSenaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetalleSenaContenido(
        uiState = uiState,
        onCerrar = onCerrar,
        onProbarConCamara = {
            onProbarConCamara(viewModel.leccionId, viewModel.senaId, viewModel.enRecorrido)
        },
        onReintentar = viewModel::reintentar,
        modifier = modifier,
    )
}

@Composable
fun DetalleSenaContenido(
    uiState: DetalleSenaUiState,
    onCerrar: () -> Unit,
    onProbarConCamara: () -> Unit,
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
                titulo = if (uiState.tituloDeLeccion.isEmpty()) {
                    null
                } else {
                    stringResource(R.string.detalle_sena_titulo, uiState.tituloDeLeccion)
                },
            )
        },
        bottomBar = {
            if (sena != null) {
                BotonPrincipal(
                    texto = stringResource(R.string.detalle_sena_probar),
                    onClick = onProbarConCamara,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = Espacio.margen, vertical = Espacio.md)
                        .fillMaxWidth(),
                )
            }
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
                mensaje = stringResource(R.string.detalle_no_encontrada),
                modifier = Modifier.padding(relleno),
            )

            else -> Column(
                verticalArrangement = Arrangement.spacedBy(Espacio.md),
                modifier = Modifier
                    .padding(relleno)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Espacio.margen, vertical = Espacio.xs),
            ) {
                Text(
                    text = sena.nombre,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.detalle_sena_indicacion),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = Espacio.xs),
                )
                Text(
                    text = sena.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                AreaDeLaSena(sena = sena, subtitulos = uiState.subtitulos)
                Text(
                    text = stringResource(R.string.detalle_sena_repite),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Donde Figma pone el clip de la seña: la imagen de referencia (RF02) sobre una superficie
 * tonal lisa (la cuadrícula está reservada al cuaderno). Si la seña todavía no tiene imagen,
 * la letra grande.
 */
@Composable
private fun AreaDeLaSena(sena: Sena, subtitulos: Boolean) {
    val descripcion = if (sena.tieneReferencia) {
        stringResource(R.string.cd_imagen_de_sena, sena.letra)
    } else {
        stringResource(R.string.cd_letra_grande, sena.letra)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(Forma.contenedorGrande)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .semantics { contentDescription = descripcion },
    ) {
        ReferenciaDeSena(
            letra = sena.letra,
            referencia = sena.referencia,
            estiloDeLetra = EstiloNumero,
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = stringResource(R.string.detalle_sena_practica_conmigo),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(Espacio.md)
                .clip(Forma.pildora)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = Espacio.sm, vertical = Espacio.xxs),
        )
        if (subtitulos) {
            Text(
                text = sena.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(Espacio.md),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DetalleSenaPreview() {
    SignaCoTheme {
        DetalleSenaContenido(
            uiState = DetalleSenaUiState(
                cargando = false,
                tituloDeLeccion = "Consonantes B, D, F y G",
                sena = Sena(
                    "W", "W", "Letra W",
                    "Índice, medio y anular estirados y separados.",
                    Categoria.CONSONANTES, 24, true,
                ),
                subtitulos = true,
            ),
            onCerrar = {},
            onProbarConCamara = {},
            onReintentar = {},
        )
    }
}
