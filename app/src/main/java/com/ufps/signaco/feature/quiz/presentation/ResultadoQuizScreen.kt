package com.ufps.signaco.feature.quiz.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.core.ui.componentes.BarraSuperior
import com.ufps.signaco.core.ui.componentes.BotonDeTexto
import com.ufps.signaco.core.ui.componentes.BotonPrincipal
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.Navegacion
import com.ufps.signaco.core.ui.componentes.Sello
import com.ufps.signaco.core.ui.componentes.Separador
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.EstiloNumero
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.quiz.domain.usecase.SenaCalificada

@Composable
fun ResultadoQuizScreen(
    onRepetirQuiz: () -> Unit,
    onRepasar: (leccionId: String, senaId: String) -> Unit,
    onVerProgreso: () -> Unit,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultadoQuizViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ResultadoQuizContenido(
        uiState = uiState,
        onRepetirQuiz = onRepetirQuiz,
        onRepasar = { senaId -> onRepasar(viewModel.leccionId, senaId) },
        onVerProgreso = onVerProgreso,
        onCerrar = onCerrar,
        modifier = modifier,
    )
}

/**
 * El número y el sello van sobre fondo liso, sin tarjeta. «Repetir el quiz» es la única
 * acción principal; ver el progreso queda como acción de texto.
 */
@Composable
fun ResultadoQuizContenido(
    uiState: ResultadoQuizUiState,
    onRepetirQuiz: () -> Unit,
    onRepasar: (String) -> Unit,
    onVerProgreso: () -> Unit,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                navegacion = Navegacion.CERRAR,
                onNavegar = onCerrar,
                titulo = if (uiState.tituloDeLeccion.isEmpty()) {
                    stringResource(R.string.resultado_titulo)
                } else {
                    stringResource(R.string.resultado_titulo_quiz, uiState.tituloDeLeccion)
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = Espacio.margen, vertical = Espacio.md),
            ) {
                BotonPrincipal(
                    texto = stringResource(R.string.resultado_repetir),
                    onClick = onRepetirQuiz,
                    modifier = Modifier.fillMaxWidth(),
                )
                BotonDeTexto(
                    texto = stringResource(R.string.resultado_ver_progreso),
                    onClick = onVerProgreso,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { relleno ->
        if (uiState.cargando) {
            EstadoDeCarga(Modifier.padding(relleno))
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = Espacio.margen, vertical = Espacio.md),
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize(),
        ) {
            item(key = "puntaje") { Puntaje(uiState) }
            if (uiState.totalImagen > 0) {
                item(key = "imagen") {
                    Text(
                        text = stringResource(
                            R.string.resultado_parte_imagen,
                            uiState.aciertosImagen,
                            uiState.totalImagen,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Espacio.xs),
                    )
                }
            }
            item(key = "detalle") {
                Text(
                    text = stringResource(
                        if (uiState.aprobado) R.string.resultado_detalle_aprobado else R.string.resultado_detalle_reprobado,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = Espacio.lg, bottom = Espacio.xs),
                )
            }
            itemsIndexed(items = uiState.senas, key = { _, it -> it.sena.id }) { indice, calificada ->
                if (indice > 0) Separador()
                FilaDeRespuesta(calificada = calificada, onRepasar = { onRepasar(calificada.sena.id) })
            }
        }
    }
}

@Composable
private fun Puntaje(uiState: ResultadoQuizUiState) {
    val descripcion = stringResource(R.string.cd_resultado_puntaje, uiState.aciertos, uiState.total)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.md),
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = descripcion },
    ) {
        Text(
            text = uiState.aciertos.toString(),
            style = EstiloNumero,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.resultado_de_total, uiState.total),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        // El sello premia el aprobado; con el quiz reprobado no se muestra.
        if (uiState.aprobado) Sello()
    }
}

@Composable
private fun FilaDeRespuesta(calificada: SenaCalificada, onRepasar: () -> Unit) {
    val estado = stringResource(if (calificada.acerto) R.string.cd_respuesta_acertada else R.string.cd_respuesta_fallada)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.md),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Medida.areaTactil)
            .padding(vertical = Espacio.sm),
    ) {
        Icon(
            painter = painterResource(if (calificada.acerto) R.drawable.ic_check_circle else R.drawable.ic_cancel),
            contentDescription = estado,
            tint = if (calificada.acerto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = calificada.sena.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            // Una letra puede salir varias veces en el quiz con cámara: cuántas la hizo bien.
            Text(
                text = stringResource(R.string.resultado_aciertos_de, calificada.aciertos, calificada.preguntas),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        when {
            !calificada.acerto -> BotonDeTexto(
                texto = stringResource(R.string.resultado_repasar),
                onClick = onRepasar,
            )
            calificada.dominada -> Text(
                text = stringResource(R.string.detalle_sena_dominada),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            else -> Text(
                text = stringResource(R.string.resultado_acertada),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ResultadoQuizPreview() {
    fun sena(letra: String) = Sena(letra, letra, "Letra $letra", Categoria.CONSONANTES, 1, true)
    SignaCoTheme {
        ResultadoQuizContenido(
            uiState = ResultadoQuizUiState(
                cargando = false,
                aciertos = 4,
                total = 5,
                aprobado = true,
                aciertosImagen = 3,
                totalImagen = 4,
                tituloDeLeccion = "Consonantes S, T, V, W, X y Y",
                senas = listOf(
                    SenaCalificada(sena("S"), preguntas = 2, aciertos = 2, dominada = false),
                    SenaCalificada(sena("T"), preguntas = 2, aciertos = 2, dominada = true),
                    SenaCalificada(sena("W"), preguntas = 1, aciertos = 0, dominada = false),
                ),
            ),
            onRepetirQuiz = {},
            onRepasar = {},
            onVerProgreso = {},
            onCerrar = {},
        )
    }
}
