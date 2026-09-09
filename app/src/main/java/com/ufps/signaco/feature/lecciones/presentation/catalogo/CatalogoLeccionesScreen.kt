package com.ufps.signaco.feature.lecciones.presentation.catalogo

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.ufps.signaco.core.ui.componentes.BarraDeAvance
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.lecciones.domain.model.CategoriaConLecciones
import com.ufps.signaco.feature.lecciones.domain.model.Leccion
import com.ufps.signaco.feature.lecciones.domain.model.LeccionConProgreso
import kotlin.math.roundToInt

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoLeccionesContenido(
    uiState: CatalogoLeccionesUiState,
    onLeccionSeleccionada: (String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.catalogo_titulo)) }) },
    ) { relleno ->
        when {
            uiState.cargando -> EstadoDeCarga(Modifier.padding(relleno))

            uiState.hayError -> EstadoDeError(
                mensaje = stringResource(R.string.comun_error_generico),
                onReintentar = onReintentar,
                modifier = Modifier.padding(relleno),
            )

            uiState.categorias.isEmpty() -> MensajeVacio(
                mensaje = stringResource(R.string.catalogo_vacio),
                modifier = Modifier.padding(relleno),
            )

            else -> LazyColumn(
                modifier = Modifier.padding(relleno).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.categorias.forEach { categoria ->
                    item(key = "categoria_${categoria.categoria.name}") {
                        EncabezadoDeCategoria(categoria)
                    }
                    items(
                        items = categoria.lecciones,
                        key = { it.leccion.id },
                    ) { leccion ->
                        TarjetaDeLeccion(
                            leccion = leccion,
                            onClick = { onLeccionSeleccionada(leccion.leccion.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoDeCategoria(categoria: CategoriaConLecciones) {
    Text(
        text = categoria.categoria.etiqueta,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun TarjetaDeLeccion(
    leccion: LeccionConProgreso,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val porcentaje = (leccion.porcentaje * 100).roundToInt()
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = leccion.leccion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (leccion.completada) {
                    // El estado no puede quedar solo en el color: va icono y texto.
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = stringResource(R.string.catalogo_completada),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            Text(
                text = leccion.leccion.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            BarraDeAvance(
                porcentaje = leccion.porcentaje,
                descripcionAccesible = stringResource(R.string.cd_avance_leccion, porcentaje),
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = stringResource(
                    R.string.catalogo_avance,
                    leccion.senasDominadas,
                    leccion.totalDeSenas,
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogoLeccionesPreview() {
    SignaCoTheme {
        CatalogoLeccionesContenido(
            uiState = CatalogoLeccionesUiState(
                cargando = false,
                categorias = listOf(
                    CategoriaConLecciones(
                        categoria = Categoria.VOCALES,
                        lecciones = listOf(
                            LeccionConProgreso(
                                leccion = Leccion(
                                    id = "leccion_vocales",
                                    titulo = "Vocales",
                                    descripcion = "Las cinco vocales del abecedario.",
                                    categoria = Categoria.VOCALES,
                                    orden = 1,
                                    senas = emptyList(),
                                ),
                                senas = emptyList(),
                                completada = true,
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
