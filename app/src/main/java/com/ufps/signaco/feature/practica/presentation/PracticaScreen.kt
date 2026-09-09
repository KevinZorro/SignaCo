package com.ufps.signaco.feature.practica.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import com.ufps.signaco.feature.practica.domain.model.TipoDeSimulacion
import kotlin.math.roundToInt

@Composable
fun PracticaScreen(
    onVolver: () -> Unit,
    onTerminar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PracticaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PracticaContenido(
        uiState = uiState,
        onVolver = onVolver,
        onSimular = viewModel::simular,
        onSiguiente = viewModel::siguienteSena,
        onTerminar = { onTerminar(viewModel.leccionId) },
        onCambiarPistas = viewModel::cambiarMostrarPistas,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticaContenido(
    uiState: PracticaUiState,
    onVolver: () -> Unit,
    onSimular: (TipoDeSimulacion) -> Unit,
    onSiguiente: () -> Unit,
    onTerminar: () -> Unit,
    onCambiarPistas: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // RNF-09: el acierto se anuncia por texto, por icono y, si el usuario lo tiene activado,
    // también por vibración. Nunca solo por color ni solo por sonido.
    val haptica = LocalHapticFeedback.current
    LaunchedEffect(uiState.retroalimentacion) {
        if (uiState.retroalimentacion is EstadoDeRetroalimentacion.Acertada &&
            uiState.retroalimentacionHaptica
        ) {
            haptica.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.practica_titulo)) },
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
        val sena = uiState.senaActual
        when {
            uiState.cargando -> EstadoDeCarga(Modifier.padding(relleno))

            uiState.hayError -> EstadoDeError(
                mensaje = stringResource(R.string.comun_error_generico),
                onReintentar = onVolver,
                modifier = Modifier.padding(relleno),
            )

            sena == null -> MensajeVacio(
                mensaje = stringResource(R.string.practica_sin_senas),
                modifier = Modifier.padding(relleno),
            )

            else -> Column(
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                VistaDeCamaraPendiente()

                Text(
                    text = stringResource(R.string.practica_sena_objetivo, sena.letra),
                    style = MaterialTheme.typography.titleLarge,
                )

                if (uiState.mostrarPistas) {
                    Text(
                        text = sena.descripcion,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                TarjetaDeRetroalimentacion(
                    estado = uiState.retroalimentacion,
                    letraObjetivo = sena.letra,
                )

                Text(
                    text = stringResource(
                        R.string.practica_aciertos_sesion,
                        uiState.aciertosDeLaSesion,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )

                ControlesDeSimulacion(
                    habilitados = !uiState.evaluando,
                    onSimular = onSimular,
                )

                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.practica_mostrar_pistas),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = uiState.mostrarPistas,
                        onCheckedChange = onCambiarPistas,
                    )
                }

                if (uiState.esLaUltima) {
                    Button(
                        onClick = onTerminar,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.practica_terminar))
                    }
                } else {
                    Button(
                        onClick = onSiguiente,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.practica_siguiente))
                    }
                }
            }
        }
    }
}

/** Hueco donde irá la vista previa de CameraX. Se reemplaza al portar el spike. */
@Composable
private fun VistaDeCamaraPendiente(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.practica_camara_placeholder),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.practica_camara_pendiente),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun TarjetaDeRetroalimentacion(
    estado: EstadoDeRetroalimentacion,
    letraObjetivo: String,
    modifier: Modifier = Modifier,
) {
    val esquema = MaterialTheme.colorScheme
    val fondo: Color
    val contenido: Color
    val icono: ImageVector?
    val mensaje: String

    when (estado) {
        EstadoDeRetroalimentacion.Esperando -> {
            fondo = esquema.surfaceVariant
            contenido = esquema.onSurfaceVariant
            icono = null
            mensaje = stringResource(R.string.practica_estado_esperando)
        }

        EstadoDeRetroalimentacion.Reconociendo -> {
            fondo = esquema.secondaryContainer
            contenido = esquema.onSecondaryContainer
            icono = Icons.Default.Refresh
            mensaje = stringResource(R.string.practica_estado_reconociendo)
        }

        is EstadoDeRetroalimentacion.Acertada -> {
            fondo = esquema.tertiaryContainer
            contenido = esquema.onTertiaryContainer
            icono = Icons.Default.CheckCircle
            mensaje = stringResource(
                R.string.practica_estado_acierto,
                estado.letra,
                (estado.confianza * 100).roundToInt(),
            )
        }

        is EstadoDeRetroalimentacion.OtraLetra -> {
            fondo = esquema.errorContainer
            contenido = esquema.onErrorContainer
            icono = Icons.Default.Warning
            mensaje = stringResource(
                R.string.practica_estado_otra_letra,
                estado.letraDetectada,
                letraObjetivo,
            )
        }

        EstadoDeRetroalimentacion.NoReconocida -> {
            fondo = esquema.errorContainer
            contenido = esquema.onErrorContainer
            icono = Icons.Default.Warning
            mensaje = stringResource(R.string.practica_estado_no_reconocida)
        }

        EstadoDeRetroalimentacion.SinMano -> {
            fondo = esquema.surfaceVariant
            contenido = esquema.onSurfaceVariant
            icono = Icons.Default.Info
            mensaje = stringResource(R.string.practica_estado_sin_mano)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        color = fondo,
        contentColor = contenido,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icono != null) {
                Icon(
                    imageVector = icono,
                    // El texto de al lado ya dice lo mismo; repetirlo duplicaría el anuncio.
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.size(12.dp))
            }
            Text(text = mensaje, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/**
 * ANDAMIO TEMPORAL. Botones para forzar cada desenlace mientras no hay cámara. Se borran
 * cuando la captura real alimente al clasificador.
 */
@Composable
private fun ControlesDeSimulacion(
    habilitados: Boolean,
    onSimular: (TipoDeSimulacion) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.practica_simulacion_titulo),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { onSimular(TipoDeSimulacion.SENA_CORRECTA) },
                enabled = habilitados,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.practica_simular_correcta))
            }
            OutlinedButton(
                onClick = { onSimular(TipoDeSimulacion.OTRA_LETRA) },
                enabled = habilitados,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.practica_simular_otra))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { onSimular(TipoDeSimulacion.NO_RECONOCIDA) },
                enabled = habilitados,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.practica_simular_no_reconocida))
            }
            OutlinedButton(
                onClick = { onSimular(TipoDeSimulacion.MANO_FUERA) },
                enabled = habilitados,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.practica_simular_sin_mano))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PracticaPreview() {
    SignaCoTheme {
        PracticaContenido(
            uiState = PracticaUiState(
                cargando = false,
                senas = listOf(
                    Sena(
                        id = "A",
                        letra = "A",
                        nombre = "Letra A",
                        descripcion = "Puño cerrado con el pulgar extendido al costado.",
                        categoria = Categoria.VOCALES,
                        orden = 1,
                        disponible = true,
                    ),
                ),
                retroalimentacion = EstadoDeRetroalimentacion.Acertada("A", 0.91f),
                aciertosDeLaSesion = 3,
            ),
            onVolver = {},
            onSimular = {},
            onSiguiente = {},
            onTerminar = {},
            onCambiarPistas = {},
        )
    }
}
