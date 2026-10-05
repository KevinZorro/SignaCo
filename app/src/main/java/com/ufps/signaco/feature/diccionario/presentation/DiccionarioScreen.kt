package com.ufps.signaco.feature.diccionario.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.FilaDeSena
import com.ufps.signaco.core.ui.componentes.Insignia
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.componentes.Separador
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme

@Composable
fun DiccionarioScreen(
    onSenaSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiccionarioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DiccionarioContenido(
        uiState = uiState,
        onCambiarConsulta = viewModel::cambiarConsulta,
        onCambiarCategoria = viewModel::cambiarCategoria,
        onSenaSeleccionada = onSenaSeleccionada,
        onReintentar = viewModel::reintentar,
        modifier = modifier,
    )
}

@Composable
fun DiccionarioContenido(
    uiState: DiccionarioUiState,
    onCambiarConsulta: (String) -> Unit,
    onCambiarCategoria: (Categoria?) -> Unit,
    onSenaSeleccionada: (String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) {
        Column(Modifier.safeDrawingPadding()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Espacio.md),
                modifier = Modifier.padding(start = Espacio.margen, end = Espacio.margen, top = Espacio.lg),
            ) {
                Text(
                    text = stringResource(R.string.diccionario_titulo),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.diccionario_subtitulo),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Buscador(consulta = uiState.consulta, onCambiarConsulta = onCambiarConsulta)
            }
            Filtros(seleccionada = uiState.categoria, onCambiarCategoria = onCambiarCategoria)

            when {
                uiState.cargando -> EstadoDeCarga()

                uiState.hayError -> EstadoDeError(
                    mensaje = stringResource(R.string.comun_error_generico),
                    onReintentar = onReintentar,
                )

                uiState.sinResultados -> MensajeVacio(stringResource(R.string.diccionario_sin_resultados))

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = Espacio.margen, vertical = Espacio.xs),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(items = uiState.senas, key = { _, it -> it.id }) { indice, sena ->
                        if (indice > 0) Separador()
                        FilaDeSena(
                            letra = sena.letra,
                            nombre = sena.nombre,
                            descripcion = sena.descripcion,
                            onClick = { onSenaSeleccionada(sena.id) },
                            insignia = if (sena.disponible) {
                                null
                            } else {
                                {
                                    Insignia(
                                        texto = stringResource(R.string.diccionario_no_disponible),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

/** Buscador sobre superficie tonal, sin contorno. */
@Composable
private fun Buscador(consulta: String, onCambiarConsulta: (String) -> Unit) {
    TextField(
        value = consulta,
        onValueChange = onCambiarConsulta,
        placeholder = {
            Text(
                text = stringResource(R.string.diccionario_buscar),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = Forma.pildora,
        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
        trailingIcon = {
            if (consulta.isNotEmpty()) {
                IconButton(onClick = { onCambiarConsulta("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.diccionario_limpiar),
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Medida.alturaDeBoton),
    )
}

/**
 * Solo el filtro elegido lleva relleno tonal; el resto queda neutro. La fila se desplaza en
 * horizontal para no salirse del ancho con la fuente grande.
 */
@Composable
private fun Filtros(seleccionada: Categoria?, onCambiarCategoria: (Categoria?) -> Unit) {
    val opciones = listOf<Pair<Categoria?, String>>(null to stringResource(R.string.diccionario_todos)) +
        Categoria.entries.map { it to it.etiqueta }
    Row(
        horizontalArrangement = Arrangement.spacedBy(Espacio.xs),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Espacio.margen, vertical = Espacio.sm),
    ) {
        opciones.forEach { (categoria, etiqueta) ->
            FilterChip(
                selected = categoria == seleccionada,
                onClick = { onCambiarCategoria(categoria) },
                label = { Text(etiqueta, style = MaterialTheme.typography.titleMedium) },
                shape = Forma.pildora,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = categoria == seleccionada,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = Color.Transparent,
                ),
                modifier = Modifier.heightIn(min = Medida.areaTactil),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DiccionarioPreview() {
    SignaCoTheme {
        DiccionarioContenido(
            uiState = DiccionarioUiState(
                cargando = false,
                categoria = Categoria.VOCALES,
                senas = listOf(
                    Sena("A", "A", "Letra A", "Puño cerrado con el pulgar extendido pegado al costado del índice.", Categoria.VOCALES, 1, true),
                    Sena("NN", "Ñ", "Letra Ñ", "Como la N con un trazo ondulado encima. Fuera del vocabulario del MVP.", Categoria.CONSONANTES, 15, false),
                ),
            ),
            onCambiarConsulta = {},
            onCambiarCategoria = {},
            onSenaSeleccionada = {},
            onReintentar = {},
        )
    }
}
