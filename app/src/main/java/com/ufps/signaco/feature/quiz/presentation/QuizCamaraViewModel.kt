package com.ufps.signaco.feature.quiz.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ufps.signaco.core.navegacion.Rutas
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.feature.practica.domain.EstabilizadorDePredicciones
import com.ufps.signaco.feature.practica.domain.model.EstadoDeReconocimiento
import com.ufps.signaco.feature.practica.domain.model.FrameDeLandmarks
import com.ufps.signaco.feature.practica.domain.model.ResultadoDeFrame
import com.ufps.signaco.feature.practica.domain.model.ResultadoIntento
import com.ufps.signaco.feature.practica.domain.usecase.CrearEstabilizadorUseCase
import com.ufps.signaco.feature.practica.domain.usecase.CuentaRegresivaUseCase
import com.ufps.signaco.feature.practica.domain.usecase.ObtenerUmbralDeSenaUseCase
import com.ufps.signaco.feature.practica.domain.usecase.ProcesarFrameUseCase
import com.ufps.signaco.feature.practica.presentation.ErrorDeReconocimiento
import com.ufps.signaco.feature.practica.presentation.EstadoDelPermiso
import com.ufps.signaco.feature.quiz.domain.model.DecisionDeIntento
import com.ufps.signaco.feature.quiz.domain.model.RespuestaCalificada
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz
import com.ufps.signaco.feature.quiz.domain.usecase.CalificarQuizDeCamaraUseCase
import com.ufps.signaco.feature.quiz.domain.usecase.EvaluarIntentoDePreguntaUseCase
import com.ufps.signaco.feature.quiz.domain.usecase.GenerarQuizDeCamaraUseCase
import com.ufps.signaco.feature.quiz.domain.usecase.RegistrarResultadoDeQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Parte 2 del quiz (RF15): cada pregunta pide hacer una letra frente a la cámara.
 *
 * El reconocimiento es el de la práctica: [ProcesarFrameUseCase] convierte los frames en
 * intentos con el estabilizador y, al confirmar uno, lo registra como intento de seña. Así el
 * quiz actualiza el progreso (practicada y dominada) sin código propio para eso.
 *
 * Qué pasa con cada intento lo decide [EvaluarIntentoDePreguntaUseCase]; la calificación y la
 * aprobación, el dominio. Aquí solo se orquesta: cámara, cuenta regresiva y avance.
 */
@HiltViewModel
class QuizCamaraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val generarQuiz: GenerarQuizDeCamaraUseCase,
    private val evaluarIntento: EvaluarIntentoDePreguntaUseCase,
    private val calificar: CalificarQuizDeCamaraUseCase,
    private val registrarResultado: RegistrarResultadoDeQuizUseCase,
    private val procesarFrame: ProcesarFrameUseCase,
    private val crearEstabilizador: CrearEstabilizadorUseCase,
    private val obtenerUmbral: ObtenerUmbralDeSenaUseCase,
    private val cuentaRegresiva: CuentaRegresivaUseCase,
    private val preferenciasRepository: PreferenciasRepository,
) : ViewModel() {

    private val ruta = savedStateHandle.toRoute<Rutas.QuizCamara>()
    val leccionId: String = ruta.leccionId

    private val _uiState = MutableStateFlow(
        QuizCamaraUiState(intentosPorPregunta = EvaluarIntentoDePreguntaUseCase.INTENTOS_POR_PREGUNTA),
    )
    val uiState: StateFlow<QuizCamaraUiState> = _uiState.asStateFlow()

    /** Deja de ser null al terminar; la pantalla lo usa para ir al resultado. */
    private val _resultado = MutableStateFlow<ResultadoQuiz?>(null)
    val resultado: StateFlow<ResultadoQuiz?> = _resultado.asStateFlow()

    private var estabilizador: EstabilizadorDePredicciones? = null
    private val hayFrameEnProceso = AtomicBoolean(false)
    private var cuentaEnCurso: Job? = null
    private var reanudacionPendiente: Job? = null
    private var esperaAntesDeReanudar = ESPERA_POR_DEFECTO_MS

    init {
        cargar()
        viewModelScope.launch {
            preferenciasRepository.observarPreferencias().collect { preferencias ->
                _uiState.update {
                    it.copy(
                        retroalimentacionHaptica = preferencias.retroalimentacionHaptica,
                        reducirAnimaciones = preferencias.reducirAnimaciones,
                    )
                }
            }
        }
    }

    fun cargar() {
        _uiState.update { it.copy(cargando = true, hayError = false) }
        viewModelScope.launch {
            runCatching {
                val preguntas = generarQuiz(leccionId)
                estabilizador = crearEstabilizador()
                preguntas
            }.onSuccess { preguntas ->
                _uiState.update { it.copy(cargando = false, preguntas = preguntas) }
                leerUmbral()
            }.onFailure { error ->
                Log.e(ETIQUETA, "No se pudo armar el quiz con cámara de $leccionId", error)
                _uiState.update { it.copy(cargando = false, hayError = true) }
            }
        }
    }

    /** Lo llama la capa de cámara por cada frame analizado. */
    fun alLlegarFrame(frame: FrameDeLandmarks) {
        val estado = _uiState.value
        if (!estado.procesandoFrames) return
        val pregunta = estado.preguntaActual ?: return
        val estabilizador = estabilizador ?: return
        if (!hayFrameEnProceso.compareAndSet(false, true)) return

        _uiState.update { it.copy(puntosDeMano = frame.puntosEnImagen, proporcionDeImagen = frame.proporcionDeImagen) }
        viewModelScope.launch {
            try {
                aplicar(procesarFrame(pregunta.senaId, frame, estabilizador))
            } catch (e: Exception) {
                Log.e(ETIQUETA, "Falló el procesamiento del frame", e)
                _uiState.update { it.copy(errorDelReconocimiento = ErrorDeReconocimiento.MODELO, capturando = false) }
            } finally {
                hayFrameEnProceso.set(false)
            }
        }
    }

    private fun aplicar(resultado: ResultadoDeFrame) {
        when (resultado) {
            is ResultadoDeFrame.Buscando -> _uiState.update {
                it.copy(
                    estadoDeReconocimiento = resultado.estado,
                    coincidencia = resultado.coincidencia,
                    noReconocida = false,
                )
            }

            // «No es una seña» se muestra, pero no gasta intento, igual que en la práctica.
            is ResultadoDeFrame.NoReconocidaEstable -> _uiState.update {
                it.copy(coincidencia = resultado.coincidencia, noReconocida = true)
            }

            is ResultadoDeFrame.Confirmado -> resolverIntento(resultado.resultado)
        }
    }

    /** Un intento confirmado (ya registrado en el progreso). */
    private fun resolverIntento(intento: ResultadoIntento) {
        if (intento is ResultadoIntento.SinMano) return
        val estado = _uiState.value
        val pregunta = estado.preguntaActual ?: return
        val usados = estado.intentosEnPregunta + 1
        val letraVista = (intento as? ResultadoIntento.OtraLetra)?.letraDetectada
        val decision = evaluarIntento(acerto = intento is ResultadoIntento.Acierto, intentosUsados = usados)

        _uiState.update {
            it.copy(
                intentosEnPregunta = usados,
                capturando = false,
                estadoDePregunta = when (decision) {
                    DecisionDeIntento.Acertada ->
                        EstadoDePregunta.Acertada((intento as ResultadoIntento.Acierto).confianza)
                    is DecisionDeIntento.OtraOportunidad ->
                        EstadoDePregunta.OtraOportunidad(letraVista, decision.intentosRestantes)
                    DecisionDeIntento.Fallada -> EstadoDePregunta.Fallada(letraVista)
                },
                respuestas = when (decision) {
                    DecisionDeIntento.Acertada -> it.respuestas + RespuestaCalificada(pregunta.senaId, true)
                    DecisionDeIntento.Fallada -> it.respuestas + RespuestaCalificada(pregunta.senaId, false)
                    is DecisionDeIntento.OtraOportunidad -> it.respuestas
                },
            )
        }
        // Con intentos de sobra, tras dejar leer el resultado vuelve a contar sola.
        if (decision is DecisionDeIntento.OtraOportunidad) {
            reanudacionPendiente?.cancel()
            reanudacionPendiente = viewModelScope.launch {
                delay(esperaAntesDeReanudar)
                intentarDeNuevo()
            }
        }
    }

    /** Otra oportunidad en la misma pregunta, tras la cuenta regresiva. */
    fun intentarDeNuevo() {
        if (_uiState.value.estadoDePregunta !is EstadoDePregunta.OtraOportunidad) return
        estabilizador?.reiniciar()
        _uiState.update { it.copy(estadoDePregunta = EstadoDePregunta.Respondiendo) }
        iniciarCuentaRegresiva()
    }

    /** La pregunta cuenta como error y se pasa a la siguiente. */
    fun saltarPregunta() {
        val estado = _uiState.value
        // Solo mientras la pregunta sigue abierta: una ya ganada o perdida ya tiene respuesta.
        if (estado.estadoDePregunta is EstadoDePregunta.Acertada ||
            estado.estadoDePregunta is EstadoDePregunta.Fallada
        ) {
            return
        }
        val pregunta = estado.preguntaActual ?: return
        _uiState.update { it.copy(respuestas = it.respuestas + RespuestaCalificada(pregunta.senaId, false)) }
        siguientePregunta()
    }

    /** Tras una pregunta resuelta: la siguiente, o el resultado si era la última. */
    fun siguientePregunta() {
        reanudacionPendiente?.cancel()
        cuentaEnCurso?.cancel()
        val estado = _uiState.value
        if (estado.esLaUltima) {
            terminar()
            return
        }
        estabilizador?.reiniciar()
        _uiState.update {
            it.copy(
                indiceActual = it.indiceActual + 1,
                intentosEnPregunta = 0,
                estadoDePregunta = EstadoDePregunta.Respondiendo,
                coincidencia = 0f,
                noReconocida = false,
            )
        }
        leerUmbral()
        iniciarCuentaRegresiva()
    }

    private fun terminar() {
        val estado = _uiState.value
        _uiState.update { it.copy(capturando = false) }
        viewModelScope.launch {
            val resultado = calificar(estado.respuestas, totalDePreguntas = estado.total)
            runCatching { registrarResultado(leccionId, resultado) }
                .onFailure { Log.e(ETIQUETA, "No se pudo guardar el resultado", it) }
            _resultado.value = resultado
        }
    }

    // --- Cámara: lo mismo que en la práctica ---

    fun alResponderElPermiso(concedido: Boolean) {
        if (concedido && _uiState.value.permiso == EstadoDelPermiso.CONCEDIDO) return
        _uiState.update {
            it.copy(
                permiso = if (concedido) EstadoDelPermiso.CONCEDIDO else EstadoDelPermiso.DENEGADO,
                capturando = false,
            )
        }
        if (concedido) iniciarCuentaRegresiva()
    }

    /** Durante la cuenta, la cancela; si no, alterna la captura (respaldo de «Iniciar intento»). */
    fun alternarCaptura() {
        val estado = _uiState.value
        cuentaEnCurso?.cancel()
        if (estado.contando) {
            _uiState.update { it.copy(cuentaRegresiva = null, capturando = false) }
            return
        }
        val capturando = !estado.capturando
        if (!capturando) estabilizador?.reiniciar()
        _uiState.update {
            it.copy(
                capturando = capturando,
                cuentaRegresiva = null,
                estadoDeReconocimiento = if (capturando) {
                    EstadoDeReconocimiento.BUSCANDO_MANOS
                } else {
                    EstadoDeReconocimiento.SIN_DETECCION
                },
                puntosDeMano = FloatArray(0),
            )
        }
    }

    fun cambiarDeCamara() {
        estabilizador?.reiniciar()
        _uiState.update { it.copy(camaraFrontal = !it.camaraFrontal) }
    }

    fun informarErrorDeCamara() {
        _uiState.update { it.copy(errorDelReconocimiento = ErrorDeReconocimiento.CAMARA, capturando = false) }
    }

    /** RNF-13: siempre se puede reintentar sin reiniciar la app. */
    fun reintentarTrasError() {
        estabilizador?.reiniciar()
        _uiState.update { it.copy(errorDelReconocimiento = null) }
        if (_uiState.value.permiso == EstadoDelPermiso.CONCEDIDO) iniciarCuentaRegresiva()
    }

    fun ajustarEsperaDeReanudacion(milisegundos: Long) {
        esperaAntesDeReanudar = milisegundos.coerceAtLeast(ESPERA_POR_DEFECTO_MS)
    }

    private fun iniciarCuentaRegresiva() {
        cuentaEnCurso?.cancel()
        _uiState.update {
            it.copy(
                capturando = false,
                estadoDeReconocimiento = EstadoDeReconocimiento.SIN_DETECCION,
                puntosDeMano = FloatArray(0),
                noReconocida = false,
                cuentaRegresiva = CuentaRegresivaUseCase.SEGUNDOS,
            )
        }
        cuentaEnCurso = viewModelScope.launch {
            cuentaRegresiva().collect { restantes ->
                _uiState.update {
                    if (restantes == CuentaRegresivaUseCase.INICIO) {
                        it.copy(
                            cuentaRegresiva = restantes,
                            capturando = true,
                            estadoDeReconocimiento = EstadoDeReconocimiento.BUSCANDO_MANOS,
                        )
                    } else {
                        it.copy(cuentaRegresiva = restantes)
                    }
                }
            }
            delay(DURACION_DEL_INICIO_MS)
            _uiState.update { it.copy(cuentaRegresiva = null) }
        }
    }

    /** El umbral es de cada letra y vive en config_rechazo.json: se dibuja como marca del medidor. */
    private fun leerUmbral() {
        val senaId = _uiState.value.preguntaActual?.senaId ?: return
        viewModelScope.launch {
            runCatching { obtenerUmbral(senaId) }
                .onSuccess { umbral -> _uiState.update { it.copy(umbral = umbral) } }
                .onFailure { Log.e(ETIQUETA, "No se pudo leer el umbral de $senaId", it) }
        }
    }

    companion object {
        const val ESPERA_POR_DEFECTO_MS = 1_200L
        private const val DURACION_DEL_INICIO_MS = 800L
        private const val ETIQUETA = "QuizCamara"
    }
}
