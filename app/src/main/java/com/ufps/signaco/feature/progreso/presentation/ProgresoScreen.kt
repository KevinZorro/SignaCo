package com.ufps.signaco.feature.progreso.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
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
import com.ufps.signaco.core.ui.componentes.BotonDeTexto
import com.ufps.signaco.core.ui.componentes.DialogoDeConfirmacion
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeCuadro
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.FilaDeCuadros
import com.ufps.signaco.core.ui.componentes.LeyendaDeProgreso
import com.ufps.signaco.core.ui.componentes.cuadriculado
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.core.ui.theme.signaCoColores
import com.ufps.signaco.feature.lecciones.domain.model.Leccion
import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso
import com.ufps.signaco.feature.lecciones.domain.model.SenaConProgreso
import com.ufps.signaco.feature.progreso.domain.model.EstadoDeSena
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import com.ufps.signaco.feature.progreso.domain.model.ResumenProgreso

@Composable
fun ProgresoScreen(
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgresoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProgresoContenido(
        uiState = uiState,
        onReintentar = viewModel::reintentar,
        onAbrirAjustes = onAbrirAjustes,
        onBorrarProgreso = viewModel::borrarProgreso,
        modifier = modifier,
    )
}

@Composable
fun ProgresoContenido(
    uiState: ProgresoUiState,
    onReintentar: () -> Unit,
    onAbrirAjustes: () -> Unit,
    onBorrarProgreso: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmandoBorrado by rememberSaveable { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) {
        when {
            uiState.cargando -> EstadoDeCarga()

            uiState.hayError -> EstadoDeError(
                mensaje = stringResource(R.string.comun_error_generico),
                onReintentar = onReintentar,
            )

            else -> Column(
                verticalArrangement = Arrangement.spacedBy(Espacio.lg),
                modifier = Modifier
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Espacio.margen, vertical = Espacio.lg),
            ) {
                Encabezado(onAbrirAjustes)
                Cuaderno(uiState)
                Racha(uiState.resumen)
                Column {
                    // Al final del contenido y en Tinta suave: el rojo queda para el diálogo.
                    BotonDeTexto(
                        texto = stringResource(R.string.progreso_borrar),
                        onClick = { confirmandoBorrado = true },
                        icono = R.drawable.ic_delete,
                        cargando = uiState.borrando,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.progreso_borrar_nota),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Espacio.md),
                    )
                }
            }
        }
    }

    if (confirmandoBorrado) {
        DialogoDeConfirmacion(
            titulo = stringResource(R.string.progreso_borrar_titulo),
            mensaje = stringResource(R.string.progreso_borrar_mensaje),
            textoConfirmar = stringResource(R.string.progreso_borrar_confirmar),
            textoCancelar = stringResource(R.string.progreso_cancelar),
            onConfirmar = {
                confirmandoBorrado = false
                onBorrarProgreso()
            },
            onCancelar = { confirmandoBorrado = false },
        )
    }
}

@Composable
private fun Encabezado(onAbrirAjustes: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Espacio.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.progreso_titulo),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            IconButton(onClick = onAbrirAjustes) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.progreso_ajustes),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = stringResource(R.string.progreso_privacidad),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** El cuaderno: una fila por lección y un cuadro por seña. Es el único lugar con cuadrícula. */
@Composable
private fun Cuaderno(uiState: ProgresoUiState) {
    val resumen = uiState.resumen
    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.md),
        modifier = Modifier
            .fillMaxWidth()
            .clip(Forma.contenedor)
            .background(MaterialTheme.colorScheme.surface)
            .cuadriculado(MaterialTheme.signaCoColores.cuadricula)
            .padding(Espacio.margen),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Espacio.xxs)) {
            Text(
                text = stringResource(R.string.progreso_dominadas_de, resumen.senasDominadas, resumen.senasDisponibles),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    R.string.progreso_lecciones_de,
                    resumen.leccionesCompletadas,
                    uiState.lecciones.size,
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        uiState.lecciones.forEach { leccion ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Espacio.md),
            ) {
                Text(
                    text = leccion.leccion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                FilaDeCuadros(
                    estados = leccion.senas.map { it.progreso.estado.aCuadro() },
                    descripcionAccesible = stringResource(
                        R.string.cd_avance_cuadros,
                        leccion.senasDominadas,
                        leccion.totalDeSenas,
                    ),
                    lado = Medida.cuadroDelCuaderno,
                )
            }
        }
        LeyendaDeProgreso(etiquetaEnCurso = stringResource(R.string.leyenda_practicada))
    }
}

/** En el cuaderno, «practicada» es lo que se está aprendiendo: lleva el amarillo. */
private fun EstadoDeSena.aCuadro(): EstadoDeCuadro = when (this) {
    EstadoDeSena.DOMINADA -> EstadoDeCuadro.DOMINADA
    EstadoDeSena.PRACTICADA -> EstadoDeCuadro.EN_CURSO
    EstadoDeSena.POR_APRENDER -> EstadoDeCuadro.POR_APRENDER
}

@Composable
private fun Racha(resumen: ResumenProgreso) {
    val iniciales = stringArrayResource(R.array.dias_iniciales)
    val nombres = stringArrayResource(R.array.dias_nombres)
    Column(verticalArrangement = Arrangement.spacedBy(Espacio.sm)) {
        Column(verticalArrangement = Arrangement.spacedBy(Espacio.xxs)) {
            Text(
                text = pluralStringResource(R.plurals.progreso_dias_seguidos, resumen.rachaActual, resumen.rachaActual),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(
                    R.string.progreso_mejor_racha_texto,
                    pluralStringResource(R.plurals.progreso_dias, resumen.mejorRacha, resumen.mejorRacha),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Espacio.xs)) {
            resumen.actividadDeLaSemana.forEachIndexed { indice, activo ->
                DiaDeLaSemana(
                    inicial = iniciales[indice],
                    descripcion = stringResource(
                        if (activo) R.string.cd_dia_activo else R.string.cd_dia_inactivo,
                        nombres[indice],
                    ),
                    activo = activo,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DiaDeLaSemana(inicial: String, descripcion: String, activo: Boolean, modifier: Modifier = Modifier) {
    val forma = Forma.pildora
    val base = Modifier
        .size(Medida.areaTactil)
        .clip(forma)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.clearAndSetSemantics { contentDescription = descripcion },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = if (activo) {
                base.background(MaterialTheme.colorScheme.primary)
            } else {
                base.border(Medida.trazoFino, MaterialTheme.signaCoColores.borde, forma)
            },
        ) {
            Text(
                text = inicial,
                style = MaterialTheme.typography.titleMedium,
                color = if (activo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun ProgresoPreview() {
    fun sena(letra: String, intentos: Int, aciertos: Int) = SenaConProgreso(
        Sena(letra, letra, "Letra $letra", "", Categoria.VOCALES, 1, true),
        ProgresoSena(letra, intentos, aciertos),
    )
    fun leccion(titulo: String, senas: List<SenaConProgreso>) = LeccionConProgreso(
        Leccion(titulo, titulo, "", Categoria.VOCALES, 1, emptyList()),
        senas,
        completada = false,
    )
    SignaCoTheme {
        ProgresoContenido(
            uiState = ProgresoUiState(
                cargando = false,
                resumen = ResumenProgreso.VACIO.copy(
                    senasDominadas = 5,
                    senasDisponibles = 19,
                    leccionesCompletadas = 1,
                    rachaActual = 5,
                    mejorRacha = 9,
                    actividadDeLaSemana = listOf(true, true, true, true, true, false, false),
                ),
                lecciones = listOf(
                    leccion("Vocales", listOf(sena("A", 6, 6), sena("I", 6, 6), sena("O", 2, 1), sena("U", 0, 0))),
                    // Peor caso: el nombre de lección más largo.
                    leccion(
                        "Consonantes S, T, V, W, X y Y",
                        listOf(sena("S", 0, 0), sena("T", 0, 0), sena("V", 0, 0), sena("W", 0, 0)),
                    ),
                ),
            ),
            onReintentar = {},
            onAbrirAjustes = {},
            onBorrarProgreso = {},
        )
    }
}
