package com.ufps.signaco.feature.ajustes.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.preferencias.domain.ManoDominante
import com.ufps.signaco.core.preferencias.domain.PreferenciasUsuario
import com.ufps.signaco.core.preferencias.domain.TamanoDeTexto
import com.ufps.signaco.core.preferencias.domain.VelocidadDeClips
import com.ufps.signaco.core.ui.componentes.BarraSuperior
import com.ufps.signaco.core.ui.componentes.EncabezadoDeGrupo
import com.ufps.signaco.core.ui.componentes.FilaDeInterruptor
import com.ufps.signaco.core.ui.componentes.FilaDeNavegacion
import com.ufps.signaco.core.ui.componentes.FilaDeSelector
import com.ufps.signaco.core.ui.componentes.GrupoDeAjustes
import com.ufps.signaco.core.ui.componentes.Navegacion
import com.ufps.signaco.core.ui.componentes.Separador
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import kotlinx.coroutines.launch

@Composable
fun AjustesScreen(
    onVolver: () -> Unit,
    onAbrirTamanoYContraste: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AjustesViewModel = hiltViewModel(),
) {
    val preferencias by viewModel.preferencias.collectAsStateWithLifecycle()
    AjustesContenido(
        preferencias = preferencias,
        onVolver = onVolver,
        onAbrirTamanoYContraste = onAbrirTamanoYContraste,
        onCambiarMano = viewModel::cambiarManoDominante,
        onCambiarVelocidad = viewModel::cambiarVelocidadDeClips,
        onCambiarVibracion = viewModel::cambiarVibracion,
        onCambiarSubtitulos = viewModel::cambiarSubtitulos,
        onCambiarRecordatorios = viewModel::cambiarRecordatorios,
        modifier = modifier,
    )
}

@Composable
fun AjustesContenido(
    preferencias: PreferenciasUsuario,
    onVolver: () -> Unit,
    onAbrirTamanoYContraste: () -> Unit,
    onCambiarMano: (ManoDominante) -> Unit,
    onCambiarVelocidad: (VelocidadDeClips) -> Unit,
    onCambiarVibracion: (Boolean) -> Unit,
    onCambiarSubtitulos: (Boolean) -> Unit,
    onCambiarRecordatorios: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var tienePermiso by remember { mutableStateOf(tienePermisoNotificaciones(contexto)) }

    // Reevalúa el permiso del sistema al volver a la pantalla (por ejemplo, desde Ajustes del sistema)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                tienePermiso = tienePermisoNotificaciones(contexto)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val mensajePermisoDenegado = stringResource(R.string.ajustes_recordatorios_permiso_denegado)
    val accionAbrirAjustes = stringResource(R.string.ajustes_recordatorios_abrir_ajustes)

    val launcherPermisoNotificacion = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { concedido ->
            tienePermiso = concedido
            if (concedido) {
                onCambiarRecordatorios(true)
            } else {
                onCambiarRecordatorios(false)
                scope.launch {
                    val resultado = snackbarHostState.showSnackbar(
                        message = mensajePermisoDenegado,
                        actionLabel = accionAbrirAjustes,
                        duration = SnackbarDuration.Short,
                    )
                    if (resultado == SnackbarResult.ActionPerformed) {
                        abrirAjustesDeNotificaciones(contexto)
                    }
                }
            }
        },
    )

    val alternarRecordatorios: (Boolean) -> Unit = { activar ->
        if (activar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !tienePermisoNotificaciones(contexto)) {
                launcherPermisoNotificacion.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onCambiarRecordatorios(true)
            }
        } else {
            onCambiarRecordatorios(false)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(navegacion = Navegacion.VOLVER, onNavegar = onVolver) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { relleno ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.lg),
            modifier = Modifier
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Espacio.margen, vertical = Espacio.xs),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Espacio.xs)) {
                Text(
                    text = stringResource(R.string.ajustes_titulo),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.ajustes_subtitulo),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column {
                EncabezadoDeGrupo(stringResource(R.string.ajustes_practica))
                GrupoDeAjustes {
                    FilaDeSelector(
                        titulo = stringResource(R.string.ajustes_mano),
                        descripcion = stringResource(R.string.ajustes_mano_descripcion),
                        opciones = ManoDominante.entries,
                        seleccionada = preferencias.manoDominante,
                        etiqueta = { stringResource(it.etiqueta()) },
                        onSeleccion = onCambiarMano,
                    )
                    Separador()
                    FilaDeSelector(
                        titulo = stringResource(R.string.ajustes_velocidad),
                        opciones = VelocidadDeClips.entries,
                        seleccionada = preferencias.velocidadDeClips,
                        etiqueta = { stringResource(it.etiqueta()) },
                        onSeleccion = onCambiarVelocidad,
                    )
                    Separador()
                    FilaDeInterruptor(
                        titulo = stringResource(R.string.ajustes_vibracion),
                        descripcion = stringResource(R.string.ajustes_vibracion_descripcion),
                        activo = preferencias.retroalimentacionHaptica,
                        onCambio = onCambiarVibracion,
                    )
                }
            }

            Column {
                EncabezadoDeGrupo(stringResource(R.string.ajustes_recordatorios))
                GrupoDeAjustes {
                    FilaDeInterruptor(
                        titulo = stringResource(R.string.ajustes_recordatorios_titulo),
                        descripcion = stringResource(R.string.ajustes_recordatorios_descripcion),
                        // El interruptor solo se ve encendido si la preferencia está activa y el permiso concedido
                        activo = preferencias.recordatoriosActivos && tienePermiso,
                        onCambio = alternarRecordatorios,
                    )
                }
            }

            Column {
                EncabezadoDeGrupo(stringResource(R.string.ajustes_accesibilidad))
                GrupoDeAjustes {
                    FilaDeNavegacion(
                        titulo = stringResource(R.string.ajustes_tamano),
                        descripcion = resumenDeApariencia(preferencias),
                        onClick = onAbrirTamanoYContraste,
                    )
                    Separador()
                    FilaDeInterruptor(
                        titulo = stringResource(R.string.ajustes_subtitulos),
                        descripcion = stringResource(R.string.ajustes_subtitulos_descripcion),
                        activo = preferencias.subtitulosEnClips,
                        onCambio = onCambiarSubtitulos,
                    )
                }
            }

            Text(
                text = stringResource(R.string.ajustes_nota),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun tienePermisoNotificaciones(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

private fun abrirAjustesDeNotificaciones(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    context.startActivity(intent)
}

/** «Texto grande · Alto contraste», como en Figma, con lo que esté activo. */
@Composable
private fun resumenDeApariencia(preferencias: PreferenciasUsuario): String {
    val partes = buildList {
        add(stringResource(preferencias.tamanoDeTexto.etiqueta()))
        if (preferencias.altoContraste) add(stringResource(R.string.tamano_contraste))
    }
    return partes.joinToString(stringResource(R.string.ajustes_separador))
}

internal fun ManoDominante.etiqueta(): Int = when (this) {
    ManoDominante.DERECHA -> R.string.ajustes_mano_derecha
    ManoDominante.IZQUIERDA -> R.string.ajustes_mano_izquierda
}

internal fun VelocidadDeClips.etiqueta(): Int = when (this) {
    VelocidadDeClips.LENTA -> R.string.ajustes_velocidad_lenta
    VelocidadDeClips.MEDIA -> R.string.ajustes_velocidad_media
    VelocidadDeClips.NORMAL -> R.string.ajustes_velocidad_normal
}

internal fun TamanoDeTexto.etiqueta(): Int = when (this) {
    TamanoDeTexto.NORMAL -> R.string.tamano_normal
    TamanoDeTexto.GRANDE -> R.string.tamano_grande
    TamanoDeTexto.MUY_GRANDE -> R.string.tamano_muy_grande
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun AjustesPreview() {
    SignaCoTheme {
        AjustesContenido(
            preferencias = PreferenciasUsuario(altoContraste = true, tamanoDeTexto = TamanoDeTexto.GRANDE),
            onVolver = {},
            onAbrirTamanoYContraste = {},
            onCambiarMano = {},
            onCambiarVelocidad = {},
            onCambiarVibracion = {},
            onCambiarSubtitulos = {},
            onCambiarRecordatorios = {},
        )
    }
}
