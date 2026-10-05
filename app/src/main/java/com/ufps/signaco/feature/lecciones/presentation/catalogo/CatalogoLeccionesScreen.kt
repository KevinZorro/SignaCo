package com.ufps.signaco.feature.lecciones.presentation.catalogo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeCuadro
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.FilaDeLeccion
import com.ufps.signaco.core.ui.componentes.LeyendaDeProgreso
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.componentes.Separador
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.lecciones.domain.model.CategoriaConLecciones
import com.ufps.signaco.feature.lecciones.domain.model.Leccion
import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso
import com.ufps.signaco.feature.lecciones.domain.model.SenaConProgreso
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena

@Composable
fun CatalogoLeccionesScreen(
    onLeccionSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogoLeccionesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogoLeccionesContenido(
        uiState = uiState,
        onLeccionSeleccionada = onLeccionSeleccionada,
        onReintentar = viewModel::reintentar,
        modifier = modifier,
    )
}

/**
 * Separado del ViewModel a propósito: así se puede previsualizar y probar con estados
 * inventados, sin Hilt.
 */
@Composable
fun CatalogoLeccionesContenido(
    uiState: CatalogoLeccionesUiState,
    onLeccionSeleccionada: (String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) {
        when {
            uiState.cargando -> EstadoDeCarga()

            uiState.hayError -> EstadoDeError(
                mensaje = stringResource(R.string.comun_error_generico),
                onReintentar = onReintentar,
            )

            uiState.categorias.isEmpty() -> MensajeVacio(stringResource(R.string.catalogo_vacio))

            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = Espacio.margen, vertical = Espacio.lg),
                modifier = Modifier.safeDrawingPadding(),
            ) {
                item(key = "encabezado") { Encabezado(uiState) }
                uiState.categorias.forEach { categoria ->
                    item(key = "categoria_${categoria.categoria.name}") {
                        Text(
                            text = categoria.categoria.etiqueta,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .padding(top = Espacio.xl, bottom = Espacio.xs)
                                .semantics { heading() },
                        )
                    }
                    itemsIndexed(items = categoria.lecciones, key = { _, it -> it.leccion.id }) { indice, leccion ->
                        if (indice > 0) Separador()
                        FilaDeLeccionConProgreso(
                            leccion = leccion,
                            onClick = { onLeccionSeleccionada(leccion.leccion.id) },
                        )
                    }
                }
                item(key = "leyenda") {
                    LeyendaDeProgreso(Modifier.padding(top = Espacio.lg))
                }
            }
        }
    }
}

@Composable
private fun Encabezado(uiState: CatalogoLeccionesUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(Espacio.xs)) {
        Text(
            text = stringResource(R.string.catalogo_titulo),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        // La racha va junto al avance y no junto al título: a 360 dp el título no cabe en una
        // línea con el chip al lado.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espacio.sm),
        ) {
            Text(
                text = stringResource(R.string.catalogo_avance, uiState.senasDominadas, uiState.senasDisponibles),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            ChipDeRacha(dias = uiState.diasDeRacha)
        }
    }
}

/** Racha de días sobre una superficie tonal, sin contorno. */
@Composable
private fun ChipDeRacha(dias: Int) {
    val descripcion = pluralStringResource(R.plurals.progreso_dias_seguidos, dias, dias)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.xs),
        modifier = Modifier
            .clip(Forma.pildora)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = Espacio.sm, vertical = Espacio.xs)
            .clearAndSetSemantics { contentDescription = descripcion },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_event_available),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = pluralStringResource(R.plurals.progreso_dias, dias, dias),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun FilaDeLeccionConProgreso(leccion: LeccionConProgreso, onClick: () -> Unit) {
    val avance = stringResource(R.string.cd_avance_cuadros, leccion.senasDominadas, leccion.totalDeSenas)
    FilaDeLeccion(
        titulo = leccion.leccion.titulo,
        // Como en Figma, el subtítulo nombra las señas de la lección.
        descripcion = leccion.senas.joinToString(", ") { it.sena.letra },
        estados = cuadrosDe(leccion),
        descripcionDelAvance = if (leccion.enCurso) {
            "${stringResource(R.string.cd_leccion_en_curso)}. $avance"
        } else {
            avance
        },
        enCurso = leccion.enCurso,
        onClick = onClick,
    )
}

/**
 * Un cuadro por seña. En la lección en curso, la primera seña sin dominar es la que se
 * aprende ahora y lleva el amarillo; el resto queda en Tinta o pendiente.
 */
private fun cuadrosDe(leccion: LeccionConProgreso): List<EstadoDeCuadro> {
    val actual = if (leccion.enCurso) leccion.senas.indexOfFirst { !it.dominada } else -1
    return leccion.senas.mapIndexed { indice, sena ->
        when {
            sena.dominada -> EstadoDeCuadro.DOMINADA
            indice == actual -> EstadoDeCuadro.EN_CURSO
            else -> EstadoDeCuadro.POR_APRENDER
        }
    }
}

private fun senaDeEjemplo(letra: String, orden: Int, intentos: Int = 0, aciertos: Int = 0) = SenaConProgreso(
    sena = Sena(letra, letra, "Letra $letra", "Descripción de $letra", Categoria.CONSONANTES, orden, true),
    progreso = ProgresoSena(letra, intentos, aciertos),
)

private fun leccionDeEjemplo(id: String, titulo: String, letras: List<String>, dominadas: Int, enCurso: Boolean) =
    LeccionConProgreso(
        leccion = Leccion(id, titulo, "", Categoria.CONSONANTES, 1, emptyList()),
        senas = letras.mapIndexed { i, letra ->
            if (i < dominadas) senaDeEjemplo(letra, i, 6, 6) else senaDeEjemplo(letra, i)
        },
        completada = dominadas == letras.size,
        enCurso = enCurso,
    )

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CatalogoLeccionesPreview() {
    SignaCoTheme {
        CatalogoLeccionesContenido(
            uiState = CatalogoLeccionesUiState(
                cargando = false,
                senasDominadas = 5,
                senasDisponibles = 19,
                diasDeRacha = 5,
                categorias = listOf(
                    CategoriaConLecciones(
                        categoria = Categoria.VOCALES,
                        lecciones = listOf(leccionDeEjemplo("v", "Vocales", listOf("A", "I", "O", "U"), 4, false)),
                    ),
                    CategoriaConLecciones(
                        categoria = Categoria.CONSONANTES,
                        lecciones = listOf(
                            leccionDeEjemplo("c1", "Consonantes B, D, F y G", listOf("B", "D", "F", "G"), 1, true),
                            // Peor caso: el nombre de lección más largo.
                            leccionDeEjemplo(
                                "c4",
                                "Consonantes S, T, V, W, X y Y con giros de muñeca",
                                listOf("S", "T", "V", "W", "X", "Y"),
                                0,
                                false,
                            ),
                        ),
                    ),
                ),
            ),
            onLeccionSeleccionada = {},
            onReintentar = {},
        )
    }
}
