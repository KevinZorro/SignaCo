package com.ufps.signaco.feature.quiz.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.componentes.BarraSuperior
import com.ufps.signaco.core.ui.componentes.BotonDeTexto
import com.ufps.signaco.core.ui.componentes.BotonPrincipal
import com.ufps.signaco.core.ui.componentes.EstadoDeCarga
import com.ufps.signaco.core.ui.componentes.EstadoDeError
import com.ufps.signaco.core.ui.componentes.HojaDeResultado
import com.ufps.signaco.core.ui.componentes.MensajeVacio
import com.ufps.signaco.core.ui.componentes.Navegacion
import com.ufps.signaco.core.ui.componentes.ProgresoSegmentado
import com.ufps.signaco.core.ui.componentes.Sello
import com.ufps.signaco.core.ui.componentes.TarjetaDePalabra
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Movimiento
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.core.ui.theme.fuenteGrande
import com.ufps.signaco.feature.practica.presentation.ErrorDeReconocimiento
import com.ufps.signaco.feature.practica.presentation.EstadoDelPermiso
import com.ufps.signaco.feature.practica.presentation.camara.ControlDeCaptura
import com.ufps.signaco.feature.practica.presentation.camara.PantallaDePermiso
import com.ufps.signaco.feature.practica.presentation.camara.ResultadoDelIntento
import com.ufps.signaco.feature.practica.presentation.camara.VisorDeCamara
import com.ufps.signaco.feature.practica.presentation.camara.VistaPreviaDeCamara
import com.ufps.signaco.feature.practica.presentation.camara.abrirAjustesDeLaApp
import com.ufps.signaco.feature.practica.presentation.camara.esperaRecomendadaParaLeer
import com.ufps.signaco.feature.practica.presentation.camara.rememberSolicitudDePermisoDeCamara
import com.ufps.signaco.feature.quiz.domain.model.PreguntaDeCamara
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz
import kotlin.math.roundToInt

/**
 * Parte 2 del quiz (RF15): hacer cada letra frente a la cámara.
 *
 * Sigue la estructura de la práctica, con dos diferencias a propósito: no se muestra la imagen
 * de referencia ni el botón de pista, porque sería darle la respuesta a la persona, y hay un
 * botón para saltar la pregunta. Pantalla fuera de Figma: revisar con Nefer.
 */
@Composable
fun QuizCamaraScreen(
    onVolver: () -> Unit,
    onQuizTerminado: (leccionId: String, resultado: ResultadoQuiz) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuizCamaraViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resultado by viewModel.resultado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    LaunchedEffect(resultado) {
        resultado?.let { onQuizTerminado(viewModel.leccionId, it) }
    }
    LaunchedEffect(contexto) {
        viewModel.ajustarEsperaDeReanudacion(
            contexto.esperaRecomendadaParaLeer(QuizCamaraViewModel.ESPERA_POR_DEFECTO_MS),
        )
    }

    val solicitarPermiso = rememberSolicitudDePermisoDeCamara(viewModel::alResponderElPermiso)
    val descripcionDeLaCamara = stringResource(R.string.cd_vista_previa)

    QuizCamaraContenido(
        uiState = uiState,
        onVolver = onVolver,
        onPermitirCamara = solicitarPermiso,
        onAbrirAjustes = { contexto.abrirAjustesDeLaApp() },
        onAlternarCaptura = viewModel::alternarCaptura,
        onCambiarCamara = viewModel::cambiarDeCamara,
        onSaltar = viewModel::saltarPregunta,
        onIntentarDeNuevo = viewModel::intentarDeNuevo,
        onSiguiente = viewModel::siguientePregunta,
        onReintentarCarga = viewModel::cargar,
        onReintentarTrasError = viewModel::reintentarTrasError,
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

/** Sin ViewModel ni cámara, para previsualizar y probar con estados armados a mano. */
@Composable
fun QuizCamaraContenido(
    uiState: QuizCamaraUiState,
    onVolver: () -> Unit,
    onPermitirCamara: () -> Unit,
    onAbrirAjustes: () -> Unit,
    onAlternarCaptura: () -> Unit,
    onCambiarCamara: () -> Unit,
    onSaltar: () -> Unit,
    onIntentarDeNuevo: () -> Unit,
    onSiguiente: () -> Unit,
    onReintentarCarga: () -> Unit,
    onReintentarTrasError: () -> Unit,
    modifier: Modifier = Modifier,
    vistaDeCamara: @Composable () -> Unit = {},
) {
    // RNF-09: el acierto se dice con texto, con el sello y, si está activado, con vibración.
    val haptica = LocalHapticFeedback.current
    LaunchedEffect(uiState.estadoDePregunta) {
        if (uiState.estadoDePregunta is EstadoDePregunta.Acertada && uiState.retroalimentacionHaptica) {
            haptica.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val pregunta = uiState.preguntaActual
    when {
        uiState.cargando -> EstadoDeCarga(modifier)

        uiState.hayError -> EstadoDeError(
            mensaje = stringResource(R.string.comun_error_generico),
            onReintentar = onReintentarCarga,
            modifier = modifier,
        )

        uiState.sinPreguntas || pregunta == null -> MensajeVacio(
            stringResource(R.string.quiz_camara_sin_preguntas),
            modifier,
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
            onReintentar = onReintentarTrasError,
            modifier = modifier,
        )

        else -> PreguntaConCamara(
            uiState = uiState,
            pregunta = pregunta,
            onVolver = onVolver,
            onAlternarCaptura = onAlternarCaptura,
            onCambiarCamara = onCambiarCamara,
            onSaltar = onSaltar,
            onIntentarDeNuevo = onIntentarDeNuevo,
            onSiguiente = onSiguiente,
            vistaDeCamara = vistaDeCamara,
            modifier = modifier,
        )
    }
}

@Composable
private fun PreguntaConCamara(
    uiState: QuizCamaraUiState,
    pregunta: PreguntaDeCamara,
    onVolver: () -> Unit,
    onAlternarCaptura: () -> Unit,
    onCambiarCamara: () -> Unit,
    onSaltar: () -> Unit,
    onIntentarDeNuevo: () -> Unit,
    onSiguiente: () -> Unit,
    vistaDeCamara: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resultado = uiState.resultadoDelIntento
    Box(modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column {
                    BarraSuperior(
                        navegacion = Navegacion.CERRAR,
                        onNavegar = onVolver,
                        titulo = stringResource(
                            R.string.quiz_camara_titulo,
                            uiState.numeroDePregunta,
                            uiState.total,
                        ),
                        acciones = {
                            IconButton(onClick = onCambiarCamara) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_cameraswitch),
                                    contentDescription = stringResource(R.string.practica_cambiar_camara),
                                )
                            }
                        },
                    )
                    ProgresoSegmentado(
                        total = uiState.total,
                        actual = uiState.indiceActual,
                        descripcionAccesible = stringResource(
                            R.string.quiz_pregunta_numero,
                            uiState.numeroDePregunta,
                            uiState.total,
                        ),
                        modifier = Modifier.padding(horizontal = Espacio.margen, vertical = Espacio.xs),
                    )
                }
            },
            bottomBar = {
                if (resultado == ResultadoDelIntento.NINGUNO) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = Espacio.margen, vertical = Espacio.sm),
                    ) {
                        ControlDeCaptura(
                            capturando = uiState.capturando,
                            contando = uiState.contando,
                            onAlternarCaptura = onAlternarCaptura,
                        )
                        BotonDeTexto(
                            texto = stringResource(R.string.quiz_camara_saltar),
                            onClick = onSaltar,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
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
                // Solo la letra: la imagen de referencia sería darle la respuesta.
                TarjetaDePalabra(
                    palabra = stringResource(R.string.quiz_camara_letra, pregunta.letra),
                    indicacion = if (fuenteGrande) {
                        null
                    } else {
                        stringResource(
                            R.string.quiz_camara_indicacion,
                            uiState.intentoActual,
                            uiState.intentosPorPregunta,
                        )
                    },
                )
                VisorDeCamara(
                    estado = uiState.estadoDelVisor,
                    resultado = resultado,
                    vistaDeCamara = vistaDeCamara,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }

        AnimatedVisibility(
            visible = resultado != ResultadoDelIntento.NINGUNO,
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
                when (val estado = uiState.estadoDePregunta) {
                    is EstadoDePregunta.Acertada -> HojaDeAcierto(uiState, pregunta, estado, onSiguiente)
                    is EstadoDePregunta.OtraOportunidad -> HojaDeOtraOportunidad(pregunta, estado, onIntentarDeNuevo)
                    is EstadoDePregunta.Fallada -> HojaDePreguntaFallada(uiState, pregunta, estado, onSiguiente)
                    EstadoDePregunta.Respondiendo -> Unit
                }
            }
        }
    }
}

@Composable
private fun HojaDeAcierto(
    uiState: QuizCamaraUiState,
    pregunta: PreguntaDeCamara,
    estado: EstadoDePregunta.Acertada,
    onSiguiente: () -> Unit,
) {
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
                    stringResource(R.string.quiz_camara_letra, pregunta.letra),
                    (estado.confianza * 100).roundToInt(),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    BotonSiguiente(uiState, onSiguiente)
}

@Composable
private fun HojaDeOtraOportunidad(
    pregunta: PreguntaDeCamara,
    estado: EstadoDePregunta.OtraOportunidad,
    onIntentarDeNuevo: () -> Unit,
) {
    TituloDeFallo(pregunta, estado.letraVista)
    Text(
        text = pluralStringResource(
            R.plurals.quiz_camara_quedan,
            estado.intentosRestantes,
            estado.intentosRestantes,
        ),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
    // La cuenta vuelve a empezar sola; el botón es para no esperar.
    BotonPrincipal(
        texto = stringResource(R.string.quiz_camara_intentar_ya),
        onClick = onIntentarDeNuevo,
        icono = R.drawable.ic_refresh,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun HojaDePreguntaFallada(
    uiState: QuizCamaraUiState,
    pregunta: PreguntaDeCamara,
    estado: EstadoDePregunta.Fallada,
    onSiguiente: () -> Unit,
) {
    TituloDeFallo(pregunta, estado.letraVista)
    Text(
        text = stringResource(R.string.quiz_camara_fallada, pregunta.letra),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
    BotonSiguiente(uiState, onSiguiente)
}

/** El rojo se queda en el ícono, como en la práctica; el texto dice qué pasó. */
@Composable
private fun TituloDeFallo(pregunta: PreguntaDeCamara, letraVista: String?) {
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
            text = if (letraVista != null) {
                stringResource(R.string.practica_estado_otra_letra, letraVista, pregunta.letra)
            } else {
                stringResource(R.string.practica_no_reconocimos)
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BotonSiguiente(uiState: QuizCamaraUiState, onSiguiente: () -> Unit) {
    BotonPrincipal(
        texto = stringResource(
            if (uiState.esLaUltima) R.string.quiz_camara_ver_resultado else R.string.quiz_camara_siguiente,
        ),
        onClick = onSiguiente,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun textoDelError(error: ErrorDeReconocimiento): Int = when (error) {
    ErrorDeReconocimiento.CAMARA -> R.string.practica_error_camara
    ErrorDeReconocimiento.MODELO -> R.string.practica_error_modelo
    ErrorDeReconocimiento.CONFIGURACION -> R.string.practica_error_configuracion
}

// --- Previews ---

private val preguntasDeEjemplo = listOf("B", "D", "F", "B", "G").map { PreguntaDeCamara(it, it) }

private fun estadoDeEjemplo(estadoDePregunta: EstadoDePregunta = EstadoDePregunta.Respondiendo, cuenta: Int? = null) =
    QuizCamaraUiState(
        cargando = false,
        preguntas = preguntasDeEjemplo,
        indiceActual = 1,
        intentosEnPregunta = 1,
        intentosPorPregunta = 3,
        estadoDePregunta = estadoDePregunta,
        permiso = EstadoDelPermiso.CONCEDIDO,
        capturando = cuenta == null,
        cuentaRegresiva = cuenta,
    )

@Composable
private fun VistaPrevia(uiState: QuizCamaraUiState) {
    SignaCoTheme {
        QuizCamaraContenido(
            uiState = uiState,
            onVolver = {},
            onPermitirCamara = {},
            onAbrirAjustes = {},
            onAlternarCaptura = {},
            onCambiarCamara = {},
            onSaltar = {},
            onIntentarDeNuevo = {},
            onSiguiente = {},
            onReintentarCarga = {},
            onReintentarTrasError = {},
        )
    }
}

@Preview(name = "Cuenta regresiva", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CuentaPreview() = VistaPrevia(estadoDeEjemplo(cuenta = 2))

@Preview(name = "Otra oportunidad", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OtraOportunidadPreview() = VistaPrevia(estadoDeEjemplo(EstadoDePregunta.OtraOportunidad("F", 2)))

@Preview(name = "Acierto", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AciertoPreview() = VistaPrevia(estadoDeEjemplo(EstadoDePregunta.Acertada(0.91f)))

@Preview(name = "Fallada", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FalladaPreview() = VistaPrevia(estadoDeEjemplo(EstadoDePregunta.Fallada(null)))
