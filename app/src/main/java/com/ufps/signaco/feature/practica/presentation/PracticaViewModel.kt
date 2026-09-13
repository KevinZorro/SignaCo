package com.ufps.signaco.feature.practica.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ufps.signaco.core.navegacion.Rutas
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.core.preferencias.domain.PreferenciasUsuario
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.feature.practica.domain.EstabilizadorDePredicciones
import com.ufps.signaco.feature.practica.domain.model.EstadoDeReconocimiento
import com.ufps.signaco.feature.practica.domain.model.FrameDeLandmarks
import com.ufps.signaco.feature.practica.domain.model.ResultadoDeFrame
import com.ufps.signaco.feature.practica.domain.model.ResultadoIntento
import com.ufps.signaco.feature.practica.domain.usecase.CrearEstabilizadorUseCase
import com.ufps.signaco.feature.practica.domain.usecase.ObtenerSenasParaPracticarUseCase
import com.ufps.signaco.feature.practica.domain.usecase.ProcesarFrameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Recibe los frames que produce la cámara y los pasa por el dominio.
 *
 * No decide si el usuario acertó ni cuándo una seña está estable: eso es
 * [ProcesarFrameUseCase] y el estabilizador. Aquí solo se traduce el resultado a algo que la
 * pantalla sabe dibujar y se descartan los frames que llegan mientras hay uno en proceso.
 */
@HiltViewModel
class PracticaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    obtenerSenas: ObtenerSenasParaPracticarUseCase,
    private val preferenciasRepository: PreferenciasRepository,
    private val procesarFrame: ProcesarFrameUseCase,
    private val crearEstabilizador: CrearEstabilizadorUseCase,
) : ViewModel() {

    val leccionId: String = savedStateHandle.toRoute<Rutas.Practica>().leccionId

    private val estadoDeSesion = MutableStateFlow(EstadoDeSesion())

    /** Un estabilizador por sesión de práctica. Nunca compartido. */
    private var estabilizador: EstabilizadorDePredicciones? = null

    /**
     * La cámara entrega hasta 30 frames por segundo y cada uno dispara una inferencia. Si ya hay
     * uno en curso, el siguiente se descarta: es preferible perder frames a encolar trabajo y
     * que la retroalimentación llegue tarde (RNF01).
     */
    private val hayFrameEnProceso = AtomicBoolean(false)

    private val senas = obtenerSenas(leccionId)
        .map { lista -> CargaDeSenas(cargando = false, senas = lista) }
        .onStart { emit(CargaDeSenas(cargando = true)) }
        .catch { error ->
            Log.e(ETIQUETA, "No se pudieron cargar las señas de $leccionId", error)
            emit(CargaDeSenas(cargando = false, hayError = true))
        }

    val uiState: StateFlow<PracticaUiState> = combine(
        senas,
        preferenciasRepository.observarPreferencias(),
        estadoDeSesion,
    ) { carga, preferencias, sesion ->
        armarEstado(carga, preferencias, sesion)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIEMPO_DE_ESPERA_MS),
        initialValue = PracticaUiState(),
    )

    init {
        viewModelScope.launch {
            runCatching { crearEstabilizador() }
                .onSuccess { nuevo ->
                    estabilizador = nuevo
                    estadoDeSesion.update { it.copy(framesParaConfirmar = nuevo.k) }
                }
                .onFailure { error ->
                    Log.e(ETIQUETA, "No se pudo leer la configuración del modelo", error)
                    estadoDeSesion.update { it.copy(error = ErrorDeReconocimiento.CONFIGURACION) }
                }
        }
    }

    /** Lo llama la capa de cámara por cada frame analizado. */
    fun alLlegarFrame(frame: FrameDeLandmarks) {
        val estado = uiState.value
        if (!estado.procesandoFrames) return
        val senaObjetivo = estado.senaActual?.id ?: return
        val estabilizador = estabilizador ?: return
        if (!hayFrameEnProceso.compareAndSet(false, true)) return

        viewModelScope.launch {
            try {
                aplicar(procesarFrame(senaObjetivo, frame, estabilizador))
            } catch (e: Exception) {
                Log.e(ETIQUETA, "Falló el procesamiento del frame", e)
                estadoDeSesion.update {
                    it.copy(error = ErrorDeReconocimiento.MODELO, capturando = false)
                }
            } finally {
                hayFrameEnProceso.set(false)
            }
        }
    }

    private fun aplicar(resultado: ResultadoDeFrame) = estadoDeSesion.update { sesion ->
        when (resultado) {
            is ResultadoDeFrame.Buscando -> sesion.copy(
                estadoDeReconocimiento = resultado.estado,
                avance = resultado.avance,
                framesParaConfirmar = resultado.total,
                retroalimentacion = when (resultado.estado) {
                    EstadoDeReconocimiento.RECONOCIENDO -> EstadoDeRetroalimentacion.Reconociendo
                    else -> EstadoDeRetroalimentacion.SinMano
                },
            )

            ResultadoDeFrame.NoReconocidaEstable -> sesion.copy(
                estadoDeReconocimiento = EstadoDeReconocimiento.RECONOCIENDO,
                retroalimentacion = EstadoDeRetroalimentacion.NoReconocida,
            )

            is ResultadoDeFrame.Confirmado -> sesion.copy(
                intentoConfirmado = true,
                estadoDeReconocimiento = EstadoDeReconocimiento.RECONOCIENDO,
                retroalimentacion = resultado.resultado.aEstadoDeUi(),
                aciertos = sesion.aciertos +
                    if (resultado.resultado is ResultadoIntento.Acierto) 1 else 0,
            )
        }
    }

    /** RF10: el mismo control inicia y detiene la captura. */
    fun alternarCaptura() {
        estadoDeSesion.update { sesion ->
            val capturando = !sesion.capturando
            if (!capturando) estabilizador?.reiniciar()
            sesion.copy(
                capturando = capturando,
                estadoDeReconocimiento = if (capturando) {
                    EstadoDeReconocimiento.BUSCANDO_MANOS
                } else {
                    EstadoDeReconocimiento.SIN_DETECCION
                },
                retroalimentacion = EstadoDeRetroalimentacion.Esperando,
            )
        }
    }

    /** RF04: alternar entre cámara frontal y trasera. */
    fun cambiarDeCamara() {
        estabilizador?.reiniciar()
        estadoDeSesion.update { it.copy(camaraFrontal = !it.camaraFrontal, avance = 0) }
    }

    fun alResponderElPermiso(concedido: Boolean) {
        estadoDeSesion.update {
            it.copy(
                permiso = if (concedido) EstadoDelPermiso.CONCEDIDO else EstadoDelPermiso.DENEGADO,
                capturando = concedido,
                estadoDeReconocimiento = if (concedido) {
                    EstadoDeReconocimiento.BUSCANDO_MANOS
                } else {
                    EstadoDeReconocimiento.SIN_DETECCION
                },
            )
        }
    }

    fun informarErrorDeCamara() {
        estadoDeSesion.update {
            it.copy(error = ErrorDeReconocimiento.CAMARA, capturando = false)
        }
    }

    /** RNF-13: siempre se puede reintentar sin reiniciar la app. */
    fun reintentar() {
        estabilizador?.reiniciar()
        estadoDeSesion.update {
            it.copy(
                error = null,
                intentoConfirmado = false,
                capturando = it.permiso == EstadoDelPermiso.CONCEDIDO,
                retroalimentacion = EstadoDeRetroalimentacion.Esperando,
            )
        }
    }

    /** RF13: reintentar la misma seña sin salir de la lección. */
    fun reintentarSena() {
        estabilizador?.reiniciar()
        estadoDeSesion.update {
            it.copy(
                intentoConfirmado = false,
                avance = 0,
                retroalimentacion = EstadoDeRetroalimentacion.Esperando,
                estadoDeReconocimiento = EstadoDeReconocimiento.BUSCANDO_MANOS,
            )
        }
    }

    fun siguienteSena() {
        estabilizador?.reiniciar()
        estadoDeSesion.update {
            it.copy(
                indice = it.indice + 1,
                intentoConfirmado = false,
                avance = 0,
                retroalimentacion = EstadoDeRetroalimentacion.Esperando,
                estadoDeReconocimiento = EstadoDeReconocimiento.BUSCANDO_MANOS,
            )
        }
    }

    fun cambiarMostrarPistas(activo: Boolean) {
        viewModelScope.launch { preferenciasRepository.cambiarMostrarPistas(activo) }
    }

    private fun armarEstado(
        carga: CargaDeSenas,
        preferencias: PreferenciasUsuario,
        sesion: EstadoDeSesion,
    ) = PracticaUiState(
        cargando = carga.cargando,
        hayError = carga.hayError,
        senas = carga.senas,
        indiceActual = sesion.indice.coerceIn(0, maxOf(0, carga.senas.lastIndex)),
        retroalimentacion = sesion.retroalimentacion,
        aciertosDeLaSesion = sesion.aciertos,
        mostrarPistas = preferencias.mostrarPistas,
        retroalimentacionHaptica = preferencias.retroalimentacionHaptica,
        permiso = sesion.permiso,
        capturando = sesion.capturando,
        camaraFrontal = sesion.camaraFrontal,
        estadoDeReconocimiento = sesion.estadoDeReconocimiento,
        avanceDeEstabilidad = sesion.avance,
        framesParaConfirmar = sesion.framesParaConfirmar,
        errorDelReconocimiento = sesion.error,
        intentoConfirmado = sesion.intentoConfirmado,
    )

    private data class CargaDeSenas(
        val cargando: Boolean = true,
        val hayError: Boolean = false,
        val senas: List<Sena> = emptyList(),
    )

    private data class EstadoDeSesion(
        val indice: Int = 0,
        val retroalimentacion: EstadoDeRetroalimentacion = EstadoDeRetroalimentacion.Esperando,
        val aciertos: Int = 0,
        val permiso: EstadoDelPermiso = EstadoDelPermiso.PENDIENTE,
        val capturando: Boolean = false,
        val camaraFrontal: Boolean = true,
        val estadoDeReconocimiento: EstadoDeReconocimiento = EstadoDeReconocimiento.SIN_DETECCION,
        val avance: Int = 0,
        val framesParaConfirmar: Int = 0,
        val error: ErrorDeReconocimiento? = null,
        val intentoConfirmado: Boolean = false,
    )

    private companion object {
        const val ETIQUETA = "Practica"
        const val TIEMPO_DE_ESPERA_MS = 5_000L
    }
}

/** Traducción del resultado de dominio al estado que sabe dibujar la pantalla. */
private fun ResultadoIntento.aEstadoDeUi(): EstadoDeRetroalimentacion = when (this) {
    is ResultadoIntento.Acierto -> EstadoDeRetroalimentacion.Acertada(letra, confianza)
    is ResultadoIntento.OtraLetra -> EstadoDeRetroalimentacion.OtraLetra(letraDetectada)
    ResultadoIntento.NoReconocida -> EstadoDeRetroalimentacion.NoReconocida
    ResultadoIntento.SinMano -> EstadoDeRetroalimentacion.SinMano
}
