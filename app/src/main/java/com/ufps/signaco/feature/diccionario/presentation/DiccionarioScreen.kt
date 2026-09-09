package com.ufps.signaco.feature.diccionario.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.theme.SignaCoTheme

@Composable
fun DiccionarioScreen(
    modifier: Modifier = Modifier,
    viewModel: DiccionarioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DiccionarioContenido(
        uiState = uiState,
        onCambiarConsulta = viewModel::cambiarConsulta,
        onReintentar = viewModel::reintentar,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiccionarioContenido(
    uiState: DiccionarioUiState,
    onCambiarConsulta: (String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.diccionario_titulo)) }) },
    ) { relleno ->
        Column(modifier = Modifier.padding(relleno).fillMaxSize()) {
            OutlinedTextField(
                value = uiState.consulta,
                onValueChange = onCambiarConsulta,
                label = { Text(stringResource(R.string.diccionario_buscar)) },
                singleLine = true,
                trailingIcon = {
                    if (uiState.consulta.isNotEmpty()) {
                        IconButton(onClick = { onCambiarConsulta("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.diccionario_limpiar),
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when {
                uiState.cargando -> EstadoDeCarga()

                uiState.hayError -> EstadoDeError(
                    mensaje = stringResource(R.string.comun_error_generico),
                    onReintentar = onReintentar,
                )

                uiState.sinResultados -> MensajeVacio(
                    mensaje = stringResource(R.string.diccionario_sin_resultados),
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = uiState.senas, key = { it.id }) { sena ->
                        FilaDeDiccionario(sena)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaDeDiccionario(sena: Sena, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = sena.letra,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = sena.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = sena.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (!sena.disponible) {
                    Text(
                        text = stringResource(R.string.diccionario_no_disponible),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiccionarioPreview() {
    SignaCoTheme {
        DiccionarioContenido(
            uiState = DiccionarioUiState(
                cargando = false,
                senas = listOf(
                    Sena(
                        id = "A",
                        letra = "A",
                        nombre = "Letra A",
                        descripcion = "Puño cerrado con el pulgar extendido al costado.",
                        categoria = Categoria.VOCALES,
                        orden = 1,
                        disponible = true,
                    ),
                    Sena(
                        id = "NN",
                        letra = "Ñ",
                        nombre = "Letra Ñ",
                        descripcion = "Como la N con un trazo ondulado encima.",
                        categoria = Categoria.CONSONANTES,
                        orden = 15,
                        disponible = false,
                    ),
                ),
            ),
            onCambiarConsulta = {},
            onReintentar = {},
        )
    }
}
