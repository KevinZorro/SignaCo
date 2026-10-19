package com.ufps.signaco.feature.quiz.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.model.ReferenciaVisual
import com.ufps.signaco.core.ui.componentes.BarraSuperior
import com.ufps.signaco.core.ui.componentes.BotonPrincipal
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.componentes.Navegacion
import com.ufps.signaco.core.ui.componentes.ReferenciaDeSena
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
                titulo = stringResource(R.string.quiz_titulo_imagen),
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
                // La descripción accesible no nombra la letra: sería darle la respuesta a TalkBack.
                val descripcionDeLaImagen = stringResource(R.string.cd_imagen_del_quiz)
                ReferenciaDeSena(
                    letra = "",
                    referencia = pregunta.referencia,
                    estiloDeLetra = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .aspectRatio(1f)
                        .clip(Forma.contenedor)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .semantics { contentDescription = descripcionDeLaImagen },
                )

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
                        referencia = ReferenciaVisual.Imagen(R.drawable.sena_a),
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
