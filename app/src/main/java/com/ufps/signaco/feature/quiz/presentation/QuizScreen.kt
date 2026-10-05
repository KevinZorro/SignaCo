package com.ufps.signaco.feature.quiz.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.componentes.BarraSuperior
import com.ufps.signaco.core.ui.componentes.BotonPrincipal
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.componentes.Navegacion
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.quiz.domain.model.PreguntaQuiz
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz

@Composable
fun QuizScreen(
    onVolver: () -> Unit,
    onQuizTerminado: (String, ResultadoQuiz) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resultado by viewModel.resultado.collectAsStateWithLifecycle()

    LaunchedEffect(resultado) {
        resultado?.let { onQuizTerminado(viewModel.leccionId, it) }
    }

    QuizContenido(
        uiState = uiState,
        onVolver = onVolver,
        onResponder = viewModel::responder,
        onSiguiente = viewModel::siguientePregunta,
        onTerminar = viewModel::terminarQuiz,
        onReintentar = viewModel::cargarQuiz,
        modifier = modifier,
    )
}

@Composable
fun QuizContenido(
    uiState: QuizUiState,
    onVolver: () -> Unit,
    onResponder: (String) -> Unit,
    onSiguiente: () -> Unit,
    onTerminar: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                navegacion = Navegacion.VOLVER,
                onNavegar = onVolver,
                titulo = stringResource(R.string.quiz_titulo),
            )
        },
    ) { relleno ->
        val pregunta = uiState.preguntaActual
        when {
            uiState.cargando -> EstadoDeCarga(Modifier.padding(relleno))

            uiState.hayError -> EstadoDeError(
                mensaje = stringResource(R.string.comun_error_generico),
                onReintentar = onReintentar,
                modifier = Modifier.padding(relleno),
            )

            pregunta == null -> MensajeVacio(
                mensaje = stringResource(R.string.quiz_sin_preguntas),
                modifier = Modifier.padding(relleno),
            )

            else -> Column(
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize()
                    .padding(horizontal = Espacio.margen, vertical = Espacio.md),
                verticalArrangement = Arrangement.spacedBy(Espacio.md),
            ) {
                Text(
                    text = stringResource(
                        R.string.quiz_pregunta_numero,
                        uiState.numeroDePregunta,
                        uiState.total,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.quiz_enunciado),
                    style = MaterialTheme.typography.titleMedium,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = Forma.contenedor,
                ) {
                    Text(
                        text = pregunta.enunciado,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(Espacio.md),
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(Espacio.xs),
                ) {
                    pregunta.opciones.forEach { opcion ->
                        OpcionDeRespuesta(
                            letra = opcion,
                            seleccionada = uiState.letraSeleccionada == opcion,
                            onSeleccionar = { onResponder(opcion) },
                        )
                    }
                }

                BotonPrincipal(
                    texto = if (uiState.esLaUltima) {
                        stringResource(R.string.quiz_terminar)
                    } else {
                        stringResource(R.string.quiz_siguiente)
                    },
                    onClick = if (uiState.esLaUltima) onTerminar else onSiguiente,
                    habilitado = uiState.letraSeleccionada != null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun OpcionDeRespuesta(
    letra: String,
    seleccionada: Boolean,
    onSeleccionar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Medida.areaTactil)
            .selectable(
                selected = seleccionada,
                role = Role.RadioButton,
                onClick = onSeleccionar,
            ),
        color = if (seleccionada) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        shape = Forma.contenedor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Espacio.md, vertical = Espacio.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = seleccionada,
                // El clic lo maneja la fila entera, para que el área táctil sea grande.
                onClick = null,
            )
            Text(
                text = letra,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = Espacio.md),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizPreview() {
    SignaCoTheme {
        QuizContenido(
            uiState = QuizUiState(
                cargando = false,
                preguntas = listOf(
                    PreguntaQuiz(
                        senaId = "A",
                        enunciado = "Puño cerrado con el pulgar extendido al costado.",
                        letraCorrecta = "A",
                        opciones = listOf("A", "E", "M", "S"),
                    ),
                ),
            ),
            onVolver = {},
            onResponder = {},
            onSiguiente = {},
            onTerminar = {},
            onReintentar = {},
        )
    }
}
