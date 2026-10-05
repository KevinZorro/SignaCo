package com.ufps.signaco.feature.lecciones.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.FilaDeSena
import com.ufps.signaco.core.ui.componentes.Insignia
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.componentes.Navegacion
import com.ufps.signaco.core.ui.componentes.Separador
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.lecciones.domain.model.Leccion
import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso
import com.ufps.signaco.feature.lecciones.domain.model.SenaConProgreso
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena

@Composable
fun DetalleLeccionScreen(
    onVolver: () -> Unit,
    onSenaSeleccionada: (leccionId: String, senaId: String) -> Unit,
    onPracticar: (String) -> Unit,
    onHacerQuiz: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleLeccionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetalleLeccionContenido(
        uiState = uiState,
        onVolver = onVolver,
        onSenaSeleccionada = { senaId -> onSenaSeleccionada(viewModel.leccionId, senaId) },
        onPracticar = { onPracticar(viewModel.leccionId) },
        onHacerQuiz = { onHacerQuiz(viewModel.leccionId) },
        onReintentar = viewModel::reintentar,
        modifier = modifier,
    )
}

@Composable
fun DetalleLeccionContenido(
    uiState: DetalleLeccionUiState,
    onVolver: () -> Unit,
    onSenaSeleccionada: (String) -> Unit,
    onPracticar: () -> Unit,
    onHacerQuiz: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val leccion = uiState.leccion
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(navegacion = Navegacion.VOLVER, onNavegar = onVolver) },
        bottomBar = {
            if (leccion != null) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = Espacio.margen, vertical = Espacio.md),
                ) {
                    BotonPrincipal(
                        texto = stringResource(R.string.detalle_empezar_practica),
                        onClick = onPracticar,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // Figma deja una sola acción principal; el quiz sigue a mano como acción
                    // discreta para no quitar un camino que ya existía.
                    BotonDeTexto(
                        texto = stringResource(R.string.detalle_hacer_quiz),
                        onClick = onHacerQuiz,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
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

            leccion == null -> MensajeVacio(
                mensaje = stringResource(R.string.detalle_no_encontrada),
                modifier = Modifier.padding(relleno),
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = Espacio.margen, vertical = Espacio.xs),
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize(),
            ) {
                item(key = "encabezado") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Espacio.sm),
                        modifier = Modifier.padding(bottom = Espacio.lg),
                    ) {
                        Text(
                            text = leccion.leccion.titulo,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            text = stringResource(
                                R.string.detalle_subtitulo,
                                leccion.totalDeSenas,
                                leccion.senasDominadas,
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                itemsIndexed(items = leccion.senas, key = { _, it -> it.sena.id }) { indice, sena ->
                    if (indice > 0) Separador()
                    FilaDeSena(
                        letra = sena.sena.letra,
                        nombre = sena.sena.nombre,
                        descripcion = sena.sena.descripcion,
                        onClick = { onSenaSeleccionada(sena.sena.id) },
                        insignia = if (sena.dominada) {
                            {
                                Insignia(
                                    texto = stringResource(R.string.detalle_sena_dominada),
                                    icono = R.drawable.ic_check,
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DetalleLeccionPreview() {
    fun sena(letra: String, orden: Int, aciertos: Int, descripcion: String) = SenaConProgreso(
        sena = Sena(letra, letra, "Letra $letra", descripcion, Categoria.VOCALES, orden, true),
        progreso = ProgresoSena(letra, intentos = aciertos, aciertos = aciertos),
    )
    SignaCoTheme {
        DetalleLeccionContenido(
            uiState = DetalleLeccionUiState(
                cargando = false,
                leccion = LeccionConProgreso(
                    // Peor caso: el título de lección más largo.
                    leccion = Leccion("c4", "Consonantes S, T, V, W, X y Y", "", Categoria.CONSONANTES, 5, emptyList()),
                    senas = listOf(
                        sena("A", 1, 6, "Puño cerrado con el pulgar extendido pegado al costado del índice."),
                        sena("W", 2, 0, "Índice, medio y anular estirados y separados."),
                    ),
                    completada = false,
                ),
            ),
            onVolver = {},
            onSenaSeleccionada = {},
            onPracticar = {},
            onHacerQuiz = {},
            onReintentar = {},
        )
    }
}
