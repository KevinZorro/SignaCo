package com.ufps.signaco.feature.progreso.presentation

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.core.ui.componentes.BarraDeAvance
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.progreso.domain.model.AvanceDeSena
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import com.ufps.signaco.feature.progreso.domain.model.ResumenProgreso
import kotlin.math.roundToInt

@Composable
fun ProgresoScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgresoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProgresoContenido(
        uiState = uiState,
        onReintentar = viewModel::reintentar,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgresoContenido(
    uiState: ProgresoUiState,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.progreso_titulo)) }) },
    ) { relleno ->
        when {
            uiState.cargando -> EstadoDeCarga(Modifier.padding(relleno))

            uiState.hayError -> EstadoDeError(
                mensaje = stringResource(R.string.comun_error_generico),
                onReintentar = onReintentar,
                modifier = Modifier.padding(relleno),
            )

            else -> {
                val resumen = uiState.resumen
                LazyColumn(
                    modifier = Modifier.padding(relleno).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "dominadas") {
                        TarjetaDeResumen(
                            titulo = stringResource(R.string.progreso_senas_dominadas),
                            valor = stringResource(
                                R.string.progreso_de_total,
                                resumen.senasDominadas,
                                resumen.senasDisponibles,
                            ),
                        ) {
                            BarraDeAvance(
                                porcentaje = resumen.porcentajeDominado,
                                descripcionAccesible = stringResource(
                                    R.string.comun_porcentaje,
                                    (resumen.porcentajeDominado * 100).roundToInt(),
                                ),
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                    item(key = "lecciones") {
                        TarjetaDeResumen(
                            titulo = stringResource(R.string.progreso_lecciones_completadas),
                            valor = resumen.leccionesCompletadas.toString(),
                        )
                    }
                    item(key = "racha") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TarjetaDeResumen(
                                titulo = stringResource(R.string.progreso_racha_actual),
                                valor = pluralStringResource(
                                    R.plurals.progreso_dias,
                                    resumen.rachaActual,
                                    resumen.rachaActual,
                                ),
                                modifier = Modifier.weight(1f),
                            )
                            TarjetaDeResumen(
                                titulo = stringResource(R.string.progreso_mejor_racha),
                                valor = pluralStringResource(
                                    R.plurals.progreso_dias,
                                    resumen.mejorRacha,
                                    resumen.mejorRacha,
                                ),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    item(key = "titulo_detalle") {
                        Text(
                            text = stringResource(R.string.progreso_detalle_titulo),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    if (resumen.avancePorSena.isEmpty()) {
                        item(key = "sin_actividad") {
                            Text(
                                text = stringResource(R.string.progreso_sin_actividad),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(
                            items = resumen.avancePorSena,
                            key = { it.sena.id },
                        ) { avance ->
                            FilaDeAvance(avance)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaDeResumen(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    contenidoExtra: @Composable (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp),
            )
            contenidoExtra?.invoke()
        }
    }
}

@Composable
private fun FilaDeAvance(avance: AvanceDeSena, modifier: Modifier = Modifier) {
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
                text = avance.sena.letra,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
            Text(
                text = stringResource(
                    R.string.detalle_sena_intentos,
                    avance.progreso.aciertos,
                    avance.progreso.intentos,
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (avance.progreso.dominada) {
                Text(
                    text = stringResource(R.string.detalle_sena_dominada),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgresoPreview() {
    SignaCoTheme {
        ProgresoContenido(
            uiState = ProgresoUiState(
                cargando = false,
                resumen = ResumenProgreso(
                    senasDominadas = 7,
                    senasDisponibles = 26,
                    leccionesCompletadas = 2,
                    rachaActual = 3,
                    mejorRacha = 5,
                    avancePorSena = listOf(
                        AvanceDeSena(
                            sena = Sena(
                                id = "A",
                                letra = "A",
                                nombre = "Letra A",
                                descripcion = "Puño cerrado con el pulgar al costado.",
                                categoria = Categoria.VOCALES,
                                orden = 1,
                                disponible = true,
                            ),
                            progreso = ProgresoSena("A", intentos = 9, aciertos = 7),
                        ),
                    ),
                ),
            ),
            onReintentar = {},
        )
    }
}
