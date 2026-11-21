package com.ufps.signaco.feature.deletreo.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.componentes.BarraSuperior
import com.ufps.signaco.core.ui.componentes.BotonPrincipal
import com.ufps.signaco.core.ui.componentes.BotonTonal
import com.ufps.signaco.core.ui.componentes.Navegacion
import com.ufps.signaco.feature.practica.presentation.camara.VistaPreviaDeCamara
import com.ufps.signaco.feature.practica.presentation.camara.rememberSolicitudDePermisoDeCamara

/**
 * Pantalla mínima del deletreo (T-21): cámara, letras confirmadas en vivo y palabras con la
 * sugerencia del decodificador. Es una herramienta de prueba: sin diseño final todavía.
 */
@Composable
fun DeletreoScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeletreoViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val solicitarPermiso = rememberSolicitudDePermisoDeCamara(viewModel::alResponderElPermiso)
    val descripcionDeLaCamara = stringResource(R.string.cd_vista_previa_deletreo)
    val pista = stringResource(R.string.deletreo_pista)

    Scaffold(
        modifier = modifier,
        topBar = {
            BarraSuperior(
                navegacion = Navegacion.VOLVER,
                onNavegar = onVolver,
                titulo = stringResource(R.string.deletreo_titulo),
            )
        },
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                estado.error -> Text(
                    text = stringResource(R.string.deletreo_error),
                    style = MaterialTheme.typography.bodyLarge,
                )
                estado.permisoConcedido != true -> {
                    Text(
                        text = stringResource(R.string.deletreo_permiso),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.deletreo_permitir),
                        onClick = solicitarPermiso,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .clip(MaterialTheme.shapes.large),
                    ) {
                        VistaPreviaDeCamara(
                            camaraFrontal = true,
                            activa = estado.capturando && !estado.cargando,
                            alLlegarFrame = viewModel::alLlegarFrame,
                            alFallar = viewModel::informarErrorDeCamara,
                            modifier = Modifier
                                .fillMaxSize()
                                .semantics { contentDescription = descripcionDeLaCamara },
                        )
                    }
                    Text(
                        text = estado.letrasEnCurso.ifEmpty { pista },
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BotonPrincipal(
                            texto = stringResource(R.string.deletreo_listo),
                            onClick = viewModel::terminarPalabra,
                            modifier = Modifier.weight(1f),
                        )
                        BotonTonal(
                            texto = stringResource(R.string.deletreo_borrar),
                            onClick = viewModel::borrarTodo,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(estado.palabras.asReversed()) { p -> FilaDePalabra(p) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaDePalabra(p: PalabraDeletreada) {
    Column {
        Text(
            text = p.sugerida ?: stringResource(R.string.deletreo_sin_coincidencia),
            style = MaterialTheme.typography.titleLarge,
        )
        val detalle = buildString {
            append(stringResource(R.string.deletreo_letras, p.letras))
            if (p.otras.isNotEmpty()) append(" · ").append(p.otras.joinToString(", "))
            if (p.ambigua) append(" · ").append(stringResource(R.string.deletreo_ambigua))
        }
        Text(text = detalle, style = MaterialTheme.typography.bodyMedium)
    }
}
