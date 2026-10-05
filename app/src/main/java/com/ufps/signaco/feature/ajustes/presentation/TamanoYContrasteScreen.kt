package com.ufps.signaco.feature.ajustes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.preferencias.domain.TamanoDeTexto
import com.ufps.signaco.core.ui.componentes.BarraSuperior
import com.ufps.signaco.core.ui.componentes.BotonPrincipal
import com.ufps.signaco.core.ui.componentes.EncabezadoDeGrupo
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.FilaDeInterruptor
import com.ufps.signaco.core.ui.componentes.GrupoDeAjustes
import com.ufps.signaco.core.ui.componentes.Navegacion
import com.ufps.signaco.core.ui.componentes.Separador
import com.ufps.signaco.core.ui.componentes.cuadriculado
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.core.ui.theme.signaCoColores

@Composable
fun TamanoYContrasteScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TamanoYContrasteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TamanoYContrasteContenido(
        uiState = uiState,
        onVolver = onVolver,
        onCambiarTamano = viewModel::cambiarTamano,
        onCambiarAltoContraste = viewModel::cambiarAltoContraste,
        onCambiarReducirAnimaciones = viewModel::cambiarReducirAnimaciones,
        onGuardar = { viewModel.guardar(onVolver) },
        modifier = modifier,
    )
}

@Composable
fun TamanoYContrasteContenido(
    uiState: TamanoYContrasteUiState,
    onVolver: () -> Unit,
    onCambiarTamano: (TamanoDeTexto) -> Unit,
    onCambiarAltoContraste: (Boolean) -> Unit,
    onCambiarReducirAnimaciones: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(navegacion = Navegacion.VOLVER, onNavegar = onVolver) },
        bottomBar = {
            BotonPrincipal(
                texto = stringResource(R.string.tamano_guardar),
                onClick = onGuardar,
                cargando = uiState.guardando,
                habilitado = !uiState.cargando,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = Espacio.margen, vertical = Espacio.md)
                    .fillMaxWidth(),
            )
        },
    ) { relleno ->
        if (uiState.cargando) {
            EstadoDeCarga(Modifier.padding(relleno))
            return@Scaffold
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.lg),
            modifier = Modifier
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Espacio.margen, vertical = Espacio.xs),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Espacio.xs)) {
                Text(
                    text = stringResource(R.string.tamano_titulo),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.tamano_subtitulo),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // La vista previa se pinta con el borrador. La escala del tema ya incluye la
            // guardada, así que aquí solo se aplica la diferencia.
            SignaCoTheme(
                altoContraste = uiState.altoContraste,
                escalaDeTexto = uiState.tamano.escala / uiState.guardado.escala,
            ) {
                VistaPrevia()
            }

            Column {
                EncabezadoDeGrupo(stringResource(R.string.tamano_texto))
                SelectorDeTamano(seleccionado = uiState.tamano, onSeleccion = onCambiarTamano)
            }

            GrupoDeAjustes {
                FilaDeInterruptor(
                    titulo = stringResource(R.string.tamano_contraste),
                    descripcion = stringResource(R.string.tamano_contraste_descripcion),
                    activo = uiState.altoContraste,
                    onCambio = onCambiarAltoContraste,
                )
                Separador()
                FilaDeInterruptor(
                    titulo = stringResource(R.string.tamano_animaciones),
                    descripcion = stringResource(R.string.tamano_animaciones_descripcion),
                    activo = uiState.reducirAnimaciones,
                    onCambio = onCambiarReducirAnimaciones,
                )
            }
        }
    }
}

/** Tarjeta de la palabra, la única superficie con cuadrícula además del cuaderno. */
@Composable
private fun VistaPrevia() {
    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.xs),
        modifier = Modifier
            .fillMaxWidth()
            .clip(Forma.contenedor)
            .background(MaterialTheme.colorScheme.surface)
            .cuadriculado(MaterialTheme.signaCoColores.cuadricula)
            .padding(Espacio.lg),
    ) {
        Text(
            text = stringResource(R.string.tamano_vista_previa),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(Forma.pieza)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = Espacio.xs, vertical = Espacio.xxs),
        )
        Text(
            text = stringResource(R.string.tamano_ejemplo_palabra),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.tamano_ejemplo_descripcion),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Cada opción muestra la «A» al tamaño que produce, además del nombre. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorDeTamano(seleccionado: TamanoDeTexto, onSeleccion: (TamanoDeTexto) -> Unit) {
    val opciones = TamanoDeTexto.entries
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        opciones.forEachIndexed { indice, tamano ->
            SegmentedButton(
                selected = tamano == seleccionado,
                onClick = { onSeleccion(tamano) },
                shape = SegmentedButtonDefaults.itemShape(indice, opciones.size),
                border = SegmentedButtonDefaults.borderStroke(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    width = Medida.trazoFino,
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.primary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                // La marca de seleccionado sobra: ya lo dicen el relleno y TalkBack.
                icon = {},
                modifier = Modifier.heightIn(min = Medida.alturaDeBoton + Espacio.lg),
                label = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.tamano_muestra),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = MaterialTheme.typography.titleMedium.fontSize * tamano.escala,
                            ),
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                        Text(stringResource(tamano.etiqueta()), style = MaterialTheme.typography.labelMedium)
                    }
                },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun TamanoYContrastePreview() {
    SignaCoTheme {
        TamanoYContrasteContenido(
            uiState = TamanoYContrasteUiState(cargando = false, tamano = TamanoDeTexto.GRANDE, altoContraste = true),
            onVolver = {},
            onCambiarTamano = {},
            onCambiarAltoContraste = {},
            onCambiarReducirAnimaciones = {},
            onGuardar = {},
        )
    }
}
