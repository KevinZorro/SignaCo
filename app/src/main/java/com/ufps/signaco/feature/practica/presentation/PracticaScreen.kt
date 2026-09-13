package com.ufps.signaco.feature.practica.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.practica.domain.model.EstadoDeReconocimiento
import com.ufps.signaco.feature.practica.presentation.camara.SolicitarPermisoDeCamara
import com.ufps.signaco.feature.practica.presentation.camara.VistaPreviaDeCamara
import kotlin.math.roundToInt

@Composable
fun PracticaScreen(
    onVolver: () -> Unit,
    onTerminar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PracticaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // RNF-11: tras un fallo la captura se reanuda sola, y ese tiempo debe respetar el ajuste
    // "tiempo para realizar una acción" del sistema. Se consulta aquí porque hace falta un
    // Context, que no entra en el ViewModel.
    LaunchedEffect(contexto) {
        viewModel.ajustarEsperaDeReanudacion(
            contexto.esperaRecomendadaParaLeer(PracticaViewModel.ESPERA_POR_DEFECTO_MS),
        )
    }

    SolicitarPermisoDeCamara(
        alResponder = viewModel::alResponderElPermiso,
        solicitar = uiState.permiso == EstadoDelPermiso.PENDIENTE,
    )

    val descripcionDeLaCamara = stringResource(R.string.cd_vista_previa)

    PracticaContenido(
        uiState = uiState,
        onVolver = onVolver,
        onAlternarCaptura = viewModel::alternarCaptura,
        onCambiarCamara = viewModel::cambiarDeCamara,
        onReintentarSena = viewModel::reintentarSena,
        onReintentar = viewModel::reintentar,
        onSiguiente = viewModel::siguienteSena,
        onTerminar = { onTerminar(viewModel.leccionId) },
        onCambiarPistas = viewModel::cambiarMostrarPistas,
        modifier = modifier,
        vistaDeCamara = {
            VistaPreviaDeCamara(
                camaraFrontal = uiState.camaraFrontal,
                activa = uiState.procesandoFrames,
                alLlegarFrame = viewModel::alLlegarFrame,
                alFallar = viewModel::informarErrorDeCamara,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .semantics { contentDescription = descripcionDeLaCamara },
            )
        },
    )
}

/**
 * Separado del ViewModel a propósito: así se puede previsualizar y probar con estados
 * inventados, sin Hilt y sin cámara. La vista de cámara entra como slot por lo mismo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticaContenido(
    uiState: PracticaUiState,
    onVolver: () -> Unit,
    onAlternarCaptura: () -> Unit,
    onCambiarCamara: () -> Unit,
    onReintentarSena: () -> Unit,
    onReintentar: () -> Unit,
    onSiguiente: () -> Unit,
    onTerminar: () -> Unit,
    onCambiarPistas: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    vistaDeCamara: @Composable () -> Unit = {},
) {
    // RNF-11: el acierto se anuncia por texto, por icono y, si el usuario lo tiene activado,
    // también por vibración (RF11). Nunca solo por color ni solo por sonido.
    val contexto = LocalContext.current
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

            uiState.permiso == EstadoDelPermiso.DENEGADO -> PermisoDenegado(
                onReintentar = onReintentar,
                onAbrirAjustes = { contexto.abrirAjustesDeLaApp() },
                modifier = Modifier.padding(relleno),
            )

            uiState.errorDelReconocimiento != null -> EstadoDeError(
                mensaje = stringResource(textoDelError(uiState.errorDelReconocimiento)),
                onReintentar = onReintentar,
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
                vistaDeCamara()

                IndicadorDeReconocimiento(uiState)

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

                ControlesDeCaptura(
                    uiState = uiState,
                    onAlternarCaptura = onAlternarCaptura,
                    onCambiarCamara = onCambiarCamara,
                    onReintentarSena = onReintentarSena,
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
                    Switch(checked = uiState.mostrarPistas, onCheckedChange = onCambiarPistas)
                }

                Button(
                    onClick = if (uiState.esLaUltima) onTerminar else onSiguiente,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text(
                        text = if (uiState.esLaUltima) {
                            stringResource(R.string.practica_terminar)
                        } else {
                            stringResource(R.string.practica_siguiente)
                        },
                    )
                }
            }
        }
    }
}

/** RF09: el estado del reconocimiento se ve en todo momento, con texto además del color. */
@Composable
private fun IndicadorDeReconocimiento(uiState: PracticaUiState, modifier: Modifier = Modifier) {
    val texto = when (uiState.estadoDeReconocimiento) {
        EstadoDeReconocimiento.SIN_DETECCION -> stringResource(R.string.practica_estado_sin_deteccion)
        EstadoDeReconocimiento.BUSCANDO_MANOS ->
            stringResource(R.string.practica_estado_buscando_manos)
        EstadoDeReconocimiento.RECONOCIENDO -> stringResource(R.string.practica_estado_reconociendo)
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = texto, style = MaterialTheme.typography.labelLarge)
        if (uiState.estadoDeReconocimiento == EstadoDeReconocimiento.RECONOCIENDO &&
            uiState.framesParaConfirmar > 0
        ) {
            val avance = uiState.avanceDeEstabilidad
            val total = uiState.framesParaConfirmar
            BarraDeAvance(
                porcentaje = avance.toFloat() / total,
                descripcionAccesible = stringResource(R.string.practica_estabilidad, avance, total),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ControlesDeCaptura(
    uiState: PracticaUiState,
    onAlternarCaptura: () -> Unit,
    onCambiarCamara: () -> Unit,
    onReintentarSena: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // RF10: un control único, visible y accesible, para iniciar y detener la captura.
        OutlinedButton(
            onClick = onAlternarCaptura,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(
                text = if (uiState.capturando) {
                    stringResource(R.string.practica_detener_captura)
                } else {
                    stringResource(R.string.practica_iniciar_captura)
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // RF04
            TextButton(
                onClick = onCambiarCamara,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.practica_cambiar_camara))
            }
            // RF13
            TextButton(
                onClick = onReintentarSena,
                enabled = uiState.intentoConfirmado,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.practica_reintentar_sena))
            }
        }
    }
}

@Composable
private fun PermisoDenegado(
    onReintentar: () -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(R.string.practica_permiso_titulo),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = stringResource(R.string.practica_permiso_explicacion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = onReintentar,
            modifier = Modifier.heightIn(min = 48.dp).padding(top = 24.dp),
        ) {
            Text(stringResource(R.string.practica_permiso_conceder))
        }
        // Cuando el sistema deja de mostrar el diálogo por haberlo denegado dos veces, pedirlo
        // otra vez no hace nada. Este es el único camino que le queda al usuario.
        TextButton(
            onClick = onAbrirAjustes,
            modifier = Modifier.heightIn(min = 48.dp).padding(top = 8.dp),
        ) {
            Text(stringResource(R.string.practica_permiso_ajustes))
        }
    }
}

private fun Context.abrirAjustesDeLaApp() {
    runCatching {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

private fun textoDelError(error: ErrorDeReconocimiento): Int = when (error) {
    ErrorDeReconocimiento.CAMARA -> R.string.practica_error_camara
    ErrorDeReconocimiento.MODELO -> R.string.practica_error_modelo
    ErrorDeReconocimiento.CONFIGURACION -> R.string.practica_error_configuracion
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

@Preview(showBackground = true)
@Composable
private fun PracticaPreview() {
    SignaCoTheme {
        PracticaContenido(
            uiState = PracticaUiState(
                cargando = false,
                permiso = EstadoDelPermiso.CONCEDIDO,
                capturando = true,
                estadoDeReconocimiento = EstadoDeReconocimiento.RECONOCIENDO,
                avanceDeEstabilidad = 2,
                framesParaConfirmar = 3,
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
            onAlternarCaptura = {},
            onCambiarCamara = {},
            onReintentarSena = {},
            onReintentar = {},
            onSiguiente = {},
            onTerminar = {},
            onCambiarPistas = {},
        )
    }
}

/**
 * Tiempo que el sistema recomienda dejar un texto en pantalla, partiendo de [base]. Devuelve
 * más si la persona tiene subido el ajuste de accesibilidad "tiempo para realizar una acción".
 * Por debajo de Android 10 esa API no existe y se usa [base] tal cual.
 */
private fun Context.esperaRecomendadaParaLeer(base: Long): Long {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return base
    val gestor = getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        ?: return base
    return gestor.getRecommendedTimeoutMillis(
        base.toInt(),
        AccessibilityManager.FLAG_CONTENT_TEXT,
    ).toLong()
}
