package com.ufps.signaco.feature.practica.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
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
import com.ufps.signaco.core.ui.componentes.BotonTonal
import com.ufps.signaco.core.ui.componentes.CuentaRegresiva
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.EstiloDeMano
import com.ufps.signaco.core.ui.componentes.HojaDeResultado
import com.ufps.signaco.core.ui.componentes.MedidorDeCoincidencia
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.componentes.Navegacion
import com.ufps.signaco.core.ui.componentes.PildoraDeEstado
import com.ufps.signaco.core.ui.componentes.ProgresoSegmentado
import com.ufps.signaco.core.ui.componentes.ReferenciaDeSena
import com.ufps.signaco.core.ui.componentes.Sello
import com.ufps.signaco.core.ui.componentes.TarjetaDePalabra
import com.ufps.signaco.core.ui.componentes.TonoDePildora
import com.ufps.signaco.core.ui.componentes.marcoDeCamara
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.Movimiento
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.core.ui.theme.fuenteGrande
import com.ufps.signaco.core.ui.theme.signaCoColores
import com.ufps.signaco.feature.practica.domain.model.EstadoDeReconocimiento
import com.ufps.signaco.feature.practica.domain.model.PasoTrasElAcierto
import com.ufps.signaco.feature.practica.presentation.camara.CapaDeMano
import com.ufps.signaco.feature.practica.presentation.camara.PantallaDePermiso
import com.ufps.signaco.feature.practica.presentation.camara.VistaPreviaDeCamara
import com.ufps.signaco.feature.practica.presentation.camara.rememberSolicitudDePermisoDeCamara
import kotlin.math.roundToInt

@Composable
fun PracticaScreen(
    onVolver: () -> Unit,
    onTerminar: (String) -> Unit,
    onVolverALaLeccion: () -> Unit,
    /** Lección completa: muestra la indicación de la siguiente letra antes de su cámara. */
    onSiguienteSena: (leccionId: String, senaId: String) -> Unit,
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

    val solicitarPermiso = rememberSolicitudDePermisoDeCamara(viewModel::alResponderElPermiso)
    val descripcionDeLaCamara = stringResource(R.string.cd_vista_previa)

    PracticaContenido(
        uiState = uiState,
        onVolver = onVolver,
        onPermitirCamara = solicitarPermiso,
        onAbrirAjustes = { contexto.abrirAjustesDeLaApp() },
        onAlternarCaptura = viewModel::alternarCaptura,
        onCambiarCamara = viewModel::cambiarDeCamara,
        onReintentarSena = viewModel::reintentarSena,
        onReintentar = viewModel::reintentar,
        onContinuar = {
            when (val paso = uiState.pasoTrasElAcierto) {
                is PasoTrasElAcierto.SiguienteSena -> onSiguienteSena(viewModel.leccionId, paso.senaId)
                PasoTrasElAcierto.TerminarLeccion -> onTerminar(viewModel.leccionId)
                PasoTrasElAcierto.VolverALaLeccion -> onVolverALaLeccion()
            }
        },
        onCambiarPistas = viewModel::cambiarMostrarPistas,
        modifier = modifier,
        vistaDeCamara = {
            VistaPreviaDeCamara(
                camaraFrontal = uiState.camaraFrontal,
                activa = uiState.procesandoFrames,
                alLlegarFrame = viewModel::alLlegarFrame,
                alFallar = viewModel::informarErrorDeCamara,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = descripcionDeLaCamara },
            )
        },
    )
}

/**
 * Separado del ViewModel a propósito: así se puede previsualizar y probar con estados
 * inventados, sin Hilt y sin cámara. La vista de cámara entra como slot por lo mismo.
 */
@Composable
fun PracticaContenido(
    uiState: PracticaUiState,
    onVolver: () -> Unit,
    onPermitirCamara: () -> Unit,
    onAbrirAjustes: () -> Unit,
    onAlternarCaptura: () -> Unit,
    onCambiarCamara: () -> Unit,
    onReintentarSena: () -> Unit,
    onReintentar: () -> Unit,
    /** El botón que aparece tras acertar. Qué hace lo dice [PracticaUiState.pasoTrasElAcierto]. */
    onContinuar: () -> Unit,
    onCambiarPistas: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    vistaDeCamara: @Composable () -> Unit = {},
) {
    // RNF-09: el acierto se anuncia por texto, por el sello y, si el usuario lo tiene activado,
    // también por vibración (RF11). Nunca solo por color ni solo por sonido.
    val haptica = LocalHapticFeedback.current
    LaunchedEffect(uiState.retroalimentacion) {
        if (uiState.retroalimentacion is EstadoDeRetroalimentacion.Acertada &&
            uiState.retroalimentacionHaptica
        ) {
            haptica.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val sena = uiState.senaActual
    when {
        uiState.cargando -> EstadoDeCarga(modifier)

        uiState.hayError -> EstadoDeError(
            mensaje = stringResource(R.string.comun_error_generico),
            onReintentar = onVolver,
            modifier = modifier,
        )

        uiState.permiso != EstadoDelPermiso.CONCEDIDO -> PantallaDePermiso(
            denegado = uiState.permiso == EstadoDelPermiso.DENEGADO,
            onPermitir = onPermitirCamara,
            onAhoraNo = onVolver,
            onAbrirAjustes = onAbrirAjustes,
            modifier = modifier,
        )

        uiState.errorDelReconocimiento != null -> EstadoDeError(
            mensaje = stringResource(textoDelError(uiState.errorDelReconocimiento)),
            onReintentar = onReintentar,
            modifier = modifier,
        )

        sena == null -> MensajeVacio(stringResource(R.string.practica_sin_senas), modifier)

        else -> Practica(
            uiState = uiState,
            sena = sena,
            onVolver = onVolver,
            onAlternarCaptura = onAlternarCaptura,
            onCambiarCamara = onCambiarCamara,
            onReintentarSena = onReintentarSena,
            onContinuar = onContinuar,
            onCambiarPistas = onCambiarPistas,
            vistaDeCamara = vistaDeCamara,
            modifier = modifier,
        )
    }
}

@Composable
private fun Practica(
    uiState: PracticaUiState,
    sena: Sena,
    onVolver: () -> Unit,
    onAlternarCaptura: () -> Unit,
    onCambiarCamara: () -> Unit,
    onReintentarSena: () -> Unit,
    onContinuar: () -> Unit,
    onCambiarPistas: (Boolean) -> Unit,
    vistaDeCamara: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resultado = resultadoDe(uiState)
    Box(modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column {
                    BarraSuperior(
                        navegacion = Navegacion.CERRAR,
                        onNavegar = onVolver,
                        // Una letra suelta no es «Seña 2 de 4»: es solo esa letra.
                        titulo = if (uiState.soloEstaSena) {
                            sena.nombre
                        } else {
                            stringResource(R.string.practica_sena_de, uiState.indiceActual + 1, uiState.senas.size)
                        },
                        acciones = {
                            IconToggleButton(checked = uiState.mostrarPistas, onCheckedChange = onCambiarPistas) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_help),
                                    contentDescription = stringResource(R.string.practica_mostrar_pistas),
                                )
                            }
                            IconButton(onClick = onCambiarCamara) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_cameraswitch),
                                    contentDescription = stringResource(R.string.practica_cambiar_camara),
                                )
                            }
                        },
                    )
                    ProgresoSegmentado(
                        total = uiState.senas.size,
                        actual = uiState.indiceActual,
                        descripcionAccesible = stringResource(
                            R.string.cd_progreso_sesion,
                            uiState.indiceActual + 1,
                            uiState.senas.size,
                        ),
                        modifier = Modifier.padding(horizontal = Espacio.margen, vertical = Espacio.xs),
                    )
                }
            },
            bottomBar = {
                if (resultado == Resultado.NINGUNO) {
                    ControlDeCaptura(
                        capturando = uiState.capturando,
                        contando = uiState.contando,
                        onAlternarCaptura = onAlternarCaptura,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = Espacio.margen, vertical = Espacio.md),
                    )
                }
            },
        ) { relleno ->
            Column(
                verticalArrangement = Arrangement.spacedBy(Espacio.md),
                modifier = Modifier
                    .padding(relleno)
                    .padding(horizontal = Espacio.margen)
                    .fillMaxSize(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Espacio.md),
                ) {
                    TarjetaDePalabra(
                        palabra = sena.nombre,
                        // Con fuente grande la indicación genérica cede su sitio al visor.
                        indicacion = if (fuenteGrande) null else stringResource(R.string.practica_indicacion),
                        modifier = Modifier.weight(1f),
                    )
                    // La pista es la imagen de referencia de la seña, si la persona la pidió.
                    if (uiState.mostrarPistas && sena.referencia != null) {
                        val descripcionDeLaPista = stringResource(R.string.cd_imagen_de_sena, sena.letra)
                        ReferenciaDeSena(
                            letra = sena.letra,
                            referencia = sena.referencia,
                            estiloDeLetra = MaterialTheme.typography.headlineLarge,
                            escala = ContentScale.Crop,
                            modifier = Modifier
                                .size(Medida.miniatura)
                                .clip(Forma.pieza)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .semantics { contentDescription = descripcionDeLaPista },
                        )
                    }
                }
                VisorDeCamara(
                    uiState = uiState,
                    resultado = resultado,
                    vistaDeCamara = vistaDeCamara,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }

        AnimatedVisibility(
            visible = resultado != Resultado.NINGUNO,
            enter = if (uiState.reducirAnimaciones) {
                EnterTransition.None
            } else {
                slideInVertically(tween(Movimiento.largo, easing = LinearOutSlowInEasing)) { it } +
                    fadeIn(tween(Movimiento.corto))
            },
            exit = if (uiState.reducirAnimaciones) ExitTransition.None else fadeOut(tween(Movimiento.corto)) +
                slideOutVertically(tween(Movimiento.medio)) { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            HojaDeResultado(Modifier.navigationBarsPadding()) {
                if (resultado == Resultado.ACIERTO) {
                    ContenidoDeAcierto(uiState, sena, onContinuar, onReintentarSena)
                } else {
                    ContenidoDeFallo(uiState, onReintentarSena)
                }
            }
        }
    }
}

/** La hoja de resultado solo sale con un intento confirmado; mientras tanto la cámara manda. */
private enum class Resultado { NINGUNO, ACIERTO, FALLO }

private fun resultadoDe(uiState: PracticaUiState): Resultado = when {
    !uiState.intentoConfirmado -> Resultado.NINGUNO
    uiState.retroalimentacion is EstadoDeRetroalimentacion.Acertada -> Resultado.ACIERTO
    else -> Resultado.FALLO
}

/** RF10: un control único para iniciar y detener. Detener es tonal: no compite con la cámara. */
@Composable
private fun ControlDeCaptura(
    capturando: Boolean,
    contando: Boolean,
    onAlternarCaptura: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (contando) {
        // Durante la cuenta regresiva el mismo control la cancela, por si la persona no está lista.
        BotonTonal(
            texto = stringResource(R.string.practica_cancelar_cuenta),
            onClick = onAlternarCaptura,
            icono = R.drawable.ic_close,
            modifier = modifier.fillMaxWidth(),
        )
    } else if (capturando) {
        BotonTonal(
            texto = stringResource(R.string.practica_detener),
            onClick = onAlternarCaptura,
            icono = R.drawable.ic_stop,
            modifier = modifier.fillMaxWidth(),
        )
    } else {
        BotonPrincipal(
            texto = stringResource(R.string.practica_iniciar_intento),
            onClick = onAlternarCaptura,
            icono = R.drawable.ic_circle,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun VisorDeCamara(
    uiState: PracticaUiState,
    resultado: Resultado,
    vistaDeCamara: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = MaterialTheme.signaCoColores
    val papel = MaterialTheme.colorScheme.surface
    // Amarillo en los puntos solo mientras se reconoce; en el fallo vuelven a neutro.
    val estiloDeMano = when (resultado) {
        Resultado.ACIERTO -> EstiloDeMano(linea = papel, relleno = papel, contorno = MaterialTheme.colorScheme.primary)
        Resultado.FALLO -> EstiloDeMano(linea = papel, relleno = colores.borde, contorno = papel)
        Resultado.NINGUNO -> EstiloDeMano(linea = papel, relleno = colores.resaltador, contorno = colores.camaraBaja)
    }

    Box(
        modifier = modifier
            .clip(Forma.contenedorGrande)
            .background(Brush.verticalGradient(listOf(colores.camaraAlta, colores.camaraBaja))),
    ) {
        vistaDeCamara()
        CapaDeMano(
            puntos = uiState.puntosDeMano,
            proporcionDeImagen = uiState.proporcionDeImagen,
            estilo = estiloDeMano,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .padding(start = Espacio.lg, end = Espacio.lg, top = Espacio.xxl + Espacio.md, bottom = Espacio.lg)
                .marcoDeCamara(colores.cuadricula),
        )
        // Píldora arriba y panel abajo en una columna: con la fuente grande el visor se achica
        // y así nunca se montan uno sobre otro.
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(Espacio.md),
        ) {
            Pildora(uiState, resultado)
            if (resultado == Resultado.NINGUNO && uiState.estadoDeReconocimiento == EstadoDeReconocimiento.RECONOCIENDO) {
                PanelDeCoincidencia(coincidencia = uiState.coincidencia, umbral = uiState.umbral)
            }
        }
        uiState.cuentaRegresiva?.let { segundos ->
            CuentaRegresiva(segundos = segundos, modifier = Modifier.align(Alignment.Center))
        }
    }
}

/** RF09: el estado del reconocimiento se ve siempre, con texto además del color. */
@Composable
private fun Pildora(uiState: PracticaUiState, resultado: Resultado, modifier: Modifier = Modifier) {
    when {
        resultado == Resultado.ACIERTO -> PildoraDeEstado(
            texto = stringResource(R.string.practica_sena_confirmada),
            tono = TonoDePildora.CONFIRMADA,
            icono = R.drawable.ic_check,
            modifier = modifier,
        )
        resultado == Resultado.FALLO ||
            uiState.retroalimentacion == EstadoDeRetroalimentacion.NoReconocida -> PildoraDeEstado(
            texto = stringResource(R.string.practica_sin_coincidencia),
            tono = TonoDePildora.NEUTRA,
            icono = R.drawable.ic_close,
            modifier = modifier,
        )
        !uiState.capturando -> PildoraDeEstado(
            texto = stringResource(R.string.practica_camara_lista),
            tono = TonoDePildora.NEUTRA,
            modifier = modifier,
        )
        uiState.estadoDeReconocimiento == EstadoDeReconocimiento.RECONOCIENDO -> PildoraDeEstado(
            texto = stringResource(R.string.practica_estado_reconociendo),
            tono = TonoDePildora.ACTIVA,
            modifier = modifier,
        )
        else -> PildoraDeEstado(
            texto = stringResource(R.string.practica_estado_buscando_manos),
            tono = TonoDePildora.ACTIVA,
            modifier = modifier,
        )
    }
}

@Composable
private fun PanelDeCoincidencia(coincidencia: Float, umbral: Float, modifier: Modifier = Modifier) {
    val papel = MaterialTheme.colorScheme.surface
    val porcentaje = (coincidencia * 100).roundToInt()
    val umbralEnPorcentaje = (umbral * 100).roundToInt()
    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.xs),
        modifier = modifier
            .fillMaxWidth()
            .clip(Forma.contenedor)
            .background(MaterialTheme.signaCoColores.panelCamara.copy(alpha = OPACIDAD_DEL_PANEL))
            .padding(Espacio.md),
    ) {
        Row {
            Text(
                text = stringResource(R.string.practica_coincidencia),
                style = MaterialTheme.typography.titleMedium,
                color = papel,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.practica_porcentaje, porcentaje),
                style = MaterialTheme.typography.titleMedium,
                color = papel,
            )
        }
        MedidorDeCoincidencia(
            coincidencia = coincidencia,
            umbral = umbral,
            descripcionAccesible = stringResource(R.string.cd_coincidencia, porcentaje, umbralEnPorcentaje),
            pista = papel.copy(alpha = OPACIDAD_DE_LA_PISTA),
            relleno = papel,
            marca = papel,
        )
        Text(
            text = stringResource(R.string.practica_umbral, umbralEnPorcentaje),
            style = MaterialTheme.typography.bodyLarge,
            color = papel,
        )
    }
}

@Composable
private fun ContenidoDeAcierto(
    uiState: PracticaUiState,
    sena: Sena,
    onContinuar: () -> Unit,
    onReintentarSena: () -> Unit,
) {
    val acertada = uiState.retroalimentacion as? EstadoDeRetroalimentacion.Acertada
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.lg),
    ) {
        Sello(animar = !uiState.reducirAnimaciones)
        Column(verticalArrangement = Arrangement.spacedBy(Espacio.xxs)) {
            Text(
                text = stringResource(R.string.practica_acertaste),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    R.string.practica_acierto_detalle,
                    sena.nombre,
                    ((acertada?.confianza ?: 0f) * 100).roundToInt(),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Column {
        BotonPrincipal(
            texto = stringResource(textoDelPaso(uiState.pasoTrasElAcierto)),
            onClick = onContinuar,
            modifier = Modifier.fillMaxWidth(),
        )
        // RF13: repetir la misma seña sin salir de la lección.
        BotonDeTexto(
            texto = stringResource(R.string.practica_reintentar_sena),
            onClick = onReintentarSena,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** El rojo se queda en el ícono del título y en el tramo de la barra. */
@Composable
private fun ContenidoDeFallo(uiState: PracticaUiState, onReintentarSena: () -> Unit) {
    val otraLetra = uiState.retroalimentacion as? EstadoDeRetroalimentacion.OtraLetra
    val letraObjetivo = uiState.senaActual?.letra.orEmpty()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.sm),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_cancel),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = if (otraLetra != null) {
                stringResource(R.string.practica_estado_otra_letra, otraLetra.letraDetectada, letraObjetivo)
            } else {
                stringResource(R.string.practica_no_reconocimos)
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
    Text(
        text = stringResource(R.string.practica_consejo),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
    // Si el modelo vio otra letra, la coincidencia con la seña objetivo es cero por definición:
    // el título ya dice qué letra vio y el medidor no aportaría nada.
    if (otraLetra == null) MedidorDelFallo(uiState)
    BotonPrincipal(
        texto = stringResource(R.string.practica_reintentar),
        onClick = onReintentarSena,
        icono = R.drawable.ic_refresh,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MedidorDelFallo(uiState: PracticaUiState) {
    val porcentaje = (uiState.coincidencia * 100).roundToInt()
    val umbral = (uiState.umbral * 100).roundToInt()
    Column(verticalArrangement = Arrangement.spacedBy(Espacio.xs)) {
        MedidorDeCoincidencia(
            coincidencia = uiState.coincidencia,
            umbral = uiState.umbral,
            descripcionAccesible = stringResource(R.string.cd_coincidencia, porcentaje, umbral),
            pista = MaterialTheme.colorScheme.outlineVariant,
            relleno = MaterialTheme.colorScheme.error,
            marca = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.practica_coincidencia_necesaria, porcentaje, umbral),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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

/** El texto del botón tras acertar, según a dónde lleve. */
private fun textoDelPaso(paso: PasoTrasElAcierto): Int = when (paso) {
    is PasoTrasElAcierto.SiguienteSena -> R.string.practica_siguiente
    PasoTrasElAcierto.TerminarLeccion -> R.string.practica_terminar
    PasoTrasElAcierto.VolverALaLeccion -> R.string.practica_volver_a_la_leccion
}

private fun textoDelError(error: ErrorDeReconocimiento): Int = when (error) {
    ErrorDeReconocimiento.CAMARA -> R.string.practica_error_camara
    ErrorDeReconocimiento.MODELO -> R.string.practica_error_modelo
    ErrorDeReconocimiento.CONFIGURACION -> R.string.practica_error_configuracion
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

private const val OPACIDAD_DEL_PANEL = 0.85f
private const val OPACIDAD_DE_LA_PISTA = 0.25f

// --- Previews: un estado por pantalla de Figma (04 a 08), con la palabra más larga. ---

private val senasDeEjemplo = listOf("A", "B", "W", "Y").mapIndexed { i, letra ->
    Sena(letra, letra, "Letra $letra", Categoria.CONSONANTES, i, true)
}

/** Una mano abierta de ejemplo, en coordenadas de imagen. */
private val manoDeEjemplo = floatArrayOf(
    0.62f, 0.62f, 0.55f, 0.58f, 0.51f, 0.53f, 0.48f, 0.49f, 0.46f, 0.45f,
    0.58f, 0.46f, 0.58f, 0.40f, 0.58f, 0.36f, 0.58f, 0.32f,
    0.63f, 0.45f, 0.64f, 0.38f, 0.65f, 0.34f, 0.66f, 0.30f,
    0.68f, 0.46f, 0.70f, 0.40f, 0.71f, 0.36f, 0.72f, 0.33f,
    0.72f, 0.49f, 0.75f, 0.45f, 0.77f, 0.42f, 0.79f, 0.39f,
)

private fun estadoDeEjemplo(
    capturando: Boolean = true,
    reconocimiento: EstadoDeReconocimiento = EstadoDeReconocimiento.BUSCANDO_MANOS,
    retro: EstadoDeRetroalimentacion = EstadoDeRetroalimentacion.Esperando,
    confirmado: Boolean = false,
    conMano: Boolean = false,
) = PracticaUiState(
    cargando = false,
    senas = senasDeEjemplo,
    indiceActual = 2,
    permiso = EstadoDelPermiso.CONCEDIDO,
    capturando = capturando,
    estadoDeReconocimiento = reconocimiento,
    retroalimentacion = retro,
    intentoConfirmado = confirmado,
    coincidencia = 0.68f,
    umbral = 0.1f,
    puntosDeMano = if (conMano) manoDeEjemplo else FloatArray(0),
    proporcionDeImagen = 0.75f,
    mostrarPistas = false,
)

@Composable
private fun VistaPreviaDePractica(uiState: PracticaUiState) {
    SignaCoTheme {
        PracticaContenido(
            uiState = uiState,
            onVolver = {},
            onPermitirCamara = {},
            onAbrirAjustes = {},
            onAlternarCaptura = {},
            onCambiarCamara = {},
            onReintentarSena = {},
            onReintentar = {},
            onContinuar = {},
            onCambiarPistas = {},
        )
    }
}

@Preview(name = "04 Cámara lista", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CamaraListaPreview() = VistaPreviaDePractica(
    estadoDeEjemplo(capturando = false, reconocimiento = EstadoDeReconocimiento.SIN_DETECCION),
)

@Preview(name = "05 Buscando la mano", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun BuscandoPreview() = VistaPreviaDePractica(estadoDeEjemplo())

@Preview(name = "06 Reconociendo", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ReconociendoPreview() = VistaPreviaDePractica(
    estadoDeEjemplo(
        reconocimiento = EstadoDeReconocimiento.RECONOCIENDO,
        retro = EstadoDeRetroalimentacion.Reconociendo,
        conMano = true,
    ),
)

@Preview(name = "07 Acierto", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AciertoPreview() = VistaPreviaDePractica(
    estadoDeEjemplo(
        reconocimiento = EstadoDeReconocimiento.RECONOCIENDO,
        retro = EstadoDeRetroalimentacion.Acertada("W", 0.93f),
        confirmado = true,
        conMano = true,
    ),
)

@Preview(name = "08 Sin coincidencia", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SinCoincidenciaPreview() = VistaPreviaDePractica(
    estadoDeEjemplo(
        reconocimiento = EstadoDeReconocimiento.RECONOCIENDO,
        retro = EstadoDeRetroalimentacion.OtraLetra("V"),
        confirmado = true,
        conMano = true,
    ),
)

@Preview(name = "01 Permiso", showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun PermisoPreview() = VistaPreviaDePractica(estadoDeEjemplo().copy(permiso = EstadoDelPermiso.PENDIENTE))
