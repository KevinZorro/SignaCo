package com.ufps.signaco.feature.lecciones.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
import com.ufps.signaco.feature.lecciones.domain.model.Leccion
import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso
import com.ufps.signaco.feature.lecciones.domain.model.SenaConProgreso
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena

@Composable
fun DetalleLeccionScreen(
    onVolver: () -> Unit,
    onPracticar: (String) -> Unit,
    onHacerQuiz: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleLeccionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetalleLeccionContenido(
        uiState = uiState,
        onVolver = onVolver,
        onPracticar = { onPracticar(viewModel.leccionId) },
        onHacerQuiz = { onHacerQuiz(viewModel.leccionId) },
        onReintentar = viewModel::reintentar,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleLeccionContenido(
    uiState: DetalleLeccionUiState,
    onVolver: () -> Unit,
    onPracticar: () -> Unit,
    onHacerQuiz: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.leccion?.leccion?.titulo.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.comun_volver),
                        )
                    }
                },
            )
        },
    ) { relleno ->
        val leccion = uiState.leccion
        when {
            uiState.cargando -> EstadoDeCarga(Modifier.padding(relleno))

            uiState.hayError -> EstadoDeError(
                mensaje = stringResource(R.string.comun_error_generico),
                onReintentar = onReintentar,
                modifier = Modifier.padding(relleno),
            )

            leccion == null -> MensajeVacio(
                mensaje = stringResource(R.string.detalle_no_encontrada),
                modifier = Modifier.padding(relleno),
            )

            else -> LazyColumn(
                modifier = Modifier.padding(relleno).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "descripcion") {
                    Text(
                        text = leccion.leccion.descripcion,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                item(key = "acciones") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onPracticar,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = stringResource(R.string.detalle_empezar_practica),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        OutlinedButton(
                            onClick = onHacerQuiz,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        ) {
                            Text(stringResource(R.string.detalle_hacer_quiz))
                        }
                    }
                }
                item(key = "titulo_senas") {
                    Text(
                        text = stringResource(R.string.detalle_senas_de_la_leccion),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(items = leccion.senas, key = { it.sena.id }) { sena ->
                    FilaDeSena(sena)
                }
            }
        }
    }
}

@Composable
private fun FilaDeSena(sena: SenaConProgreso, modifier: Modifier = Modifier) {
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
                text = sena.sena.letra,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sena.sena.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = if (sena.progreso.intentos == 0) {
                        stringResource(R.string.detalle_sena_sin_intentos)
                    } else {
                        stringResource(
                            R.string.detalle_sena_intentos,
                            sena.progreso.aciertos,
                            sena.progreso.intentos,
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (sena.dominada) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.cd_sena_dominada),
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleLeccionPreview() {
    SignaCoTheme {
        DetalleLeccionContenido(
            uiState = DetalleLeccionUiState(
                cargando = false,
                leccion = LeccionConProgreso(
                    leccion = Leccion(
                        id = "leccion_vocales",
                        titulo = "Vocales",
                        descripcion = "Las cinco vocales del abecedario.",
                        categoria = Categoria.VOCALES,
                        orden = 1,
                        senas = emptyList(),
                    ),
                    senas = listOf(
                        SenaConProgreso(
                            sena = Sena(
                                id = "A",
                                letra = "A",
                                nombre = "Letra A",
                                descripcion = "Puño cerrado con el pulgar al costado.",
                                categoria = Categoria.VOCALES,
                                orden = 1,
                                disponible = true,
                            ),
                            progreso = ProgresoSena("A", intentos = 8, aciertos = 6),
                        ),
                    ),
                    completada = false,
                ),
            ),
            onVolver = {},
            onPracticar = {},
            onHacerQuiz = {},
            onReintentar = {},
        )
    }
}
