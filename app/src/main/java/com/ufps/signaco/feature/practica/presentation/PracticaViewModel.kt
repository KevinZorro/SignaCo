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
import com.ufps.signaco.feature.practica.domain.usecase.DecidirPasoTrasElAciertoUseCase
import com.ufps.signaco.feature.practica.domain.usecase.ObtenerSenasParaPracticarUseCase
import com.ufps.signaco.feature.practica.domain.usecase.ObtenerUmbralDeSenaUseCase
import com.ufps.signaco.feature.practica.domain.usecase.ProcesarFrameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
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
    private val obtenerUmbral: ObtenerUmbralDeSenaUseCase,
    private val decidirPasoTrasElAcierto: DecidirPasoTrasElAciertoUseCase,
) : ViewModel() {

    private val ruta = savedStateHandle.toRoute<Rutas.Practica>()
    val leccionId: String = ruta.leccionId

    private val estadoDeSesion = MutableStateFlow(EstadoDeSesion())

    /** Un estabilizador por sesión de práctica. Nunca compartido. */
    private var estabilizador: EstabilizadorDePredicciones? = null

    /**
     * La cámara entrega hasta 30 frames por segundo y cada uno dispara una inferencia. Si ya hay
     * uno en curso, el siguiente se descarta: es preferible perder frames a encolar trabajo y
     * que la retroalimentación llegue tarde (RNF01).
     */
    private val hayFrameEnProceso = AtomicBoolean(false)

    /** La reanudación pendiente tras un intento fallido. Ver [programarReanudacion]. */
    private var reanudacionPendiente: Job? = null

    /**
     * Cuánto se deja la retroalimentación en pantalla antes de reanudar sola. La pantalla la
     * ajusta al arrancar con el tiempo que el sistema recomienda para leer un texto, que es más
     * largo si la persona tiene activado "tiempo para realizar una acción" (RNF-11).
     */
    private var esperaAntesDeReanudar = ESPERA_POR_DEFECTO_MS

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
        // El umbral cambia con cada seña: se vuelve a leer del config cuando cambia la actual.
        viewModelScope.launch {
            uiState.map { it.senaActual?.id }.distinctUntilChanged().filterNotNull().collect { senaId ->
                runCatching { obtenerUmbral(senaId) }
                    .onSuccess { umbral -> estadoDeSesion.update { it.copy(umbral = umbral) } }
                    .onFailure { Log.e(ETIQUETA, "No se pudo leer el umbral de $senaId", it) }
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

        estadoDeSesion.update {
            it.copy(puntosDeMano = frame.puntosEnImagen, proporcionDeImagen = frame.proporcionDeImagen)
        }
        viewModelScope.launch {
            try {
                val resultado = procesarFrame(senaObjetivo, frame, estabilizador)
                aplicar(resultado)
                programarReanudacion(resultado)
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
                coincidencia = resultado.coincidencia,
                retroalimentacion = when (resultado.estado) {
                    EstadoDeReconocimiento.RECONOCIENDO -> EstadoDeRetroalimentacion.Reconociendo
                    else -> EstadoDeRetroalimentacion.SinMano
                },
            )

            is ResultadoDeFrame.NoReconocidaEstable -> sesion.copy(
                estadoDeReconocimiento = EstadoDeReconocimiento.RECONOCIENDO,
                coincidencia = resultado.coincidencia,
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

    /**
     * Tras un intento FALLIDO la captura se reanuda sola. Sin esto había que pulsar "volver a
     * intentar" después de cada error, lo que corta el flujo justo donde más se practica.
     *
     * Un acierto sí sigue pausando: ahí la persona quiere leerlo y decidir si avanza.
     *
     * **No se reinicia el estabilizador**, y es a propósito. Su pestillo hace que sostener la
     * misma seña equivocada NO vuelva a registrarse como intento; solo cuenta uno nuevo si la
     * pose cambia, o si se baja la mano y se vuelve a subir, que es justo lo que es un intento
     * nuevo. Reiniciarlo aquí llenaría el progreso de fallos repetidos por estar quieto.
     *
     * El botón de reintentar se queda: reanuda en el acto, sin esperar.
     */
    private fun programarReanudacion(resultado: ResultadoDeFrame) {
        if (resultado !is ResultadoDeFrame.Confirmado) return
        if (resultado.resultado is ResultadoIntento.Acierto) return

        reanudacionPendiente?.cancel()
        reanudacionPendiente = viewModelScope.launch {
            delay(esperaAntesDeReanudar)
            estadoDeSesion.update { sesion ->
                // Si mientras tanto se avanzó, se reintentó a mano o se paró la captura, no hay
                // nada que reanudar.
                if (!sesion.intentoConfirmado) {
                    sesion
                } else {
                    sesion.copy(
                        intentoConfirmado = false,
                        estadoDeReconocimiento = EstadoDeReconocimiento.BUSCANDO_MANOS,
                    )
                }
            }
        }
    }

    /**
     * La pantalla informa del tiempo que el sistema recomienda para leer un texto. Se consulta
     * allí porque hace falta un Context, que no entra en el ViewModel.
     */
    fun ajustarEsperaDeReanudacion(milisegundos: Long) {
        esperaAntesDeReanudar = milisegundos.coerceAtLeast(ESPERA_POR_DEFECTO_MS)
    }

    /** RF10: el mismo control inicia y detiene la captura. */
    fun alternarCaptura() {
        reanudacionPendiente?.cancel()
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
                puntosDeMano = FloatArray(0),
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
            // Con el permiso concedido la cámara queda lista, pero el intento lo inicia la
            // persona con el botón de iniciar.
            it.copy(
                permiso = if (concedido) EstadoDelPermiso.CONCEDIDO else EstadoDelPermiso.DENEGADO,
                capturando = false,
                estadoDeReconocimiento = EstadoDeReconocimiento.SIN_DETECCION,
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
        reanudacionPendiente?.cancel()
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
        reanudacionPendiente?.cancel()
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

    fun cambiarMostrarPistas(activo: Boolean) {
        viewModelScope.launch { preferenciasRepository.cambiarMostrarPistas(activo) }
    }

    private fun armarEstado(
        carga: CargaDeSenas,
        preferencias: PreferenciasUsuario,
        sesion: EstadoDeSesion,
    ): PracticaUiState {
        val indiceActual = indiceResuelto(sesion.indice, carga.senas)
        return PracticaUiState(
            cargando = carga.cargando,
            hayError = carga.hayError,
            senas = carga.senas,
            indiceActual = indiceActual,
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
            coincidencia = sesion.coincidencia,
            umbral = sesion.umbral,
            puntosDeMano = sesion.puntosDeMano,
            proporcionDeImagen = sesion.proporcionDeImagen,
            reducirAnimaciones = preferencias.reducirAnimaciones,
            soloEstaSena = ruta.soloEstaSena,
            pasoTrasElAcierto = decidirPasoTrasElAcierto(
                senas = carga.senas,
                senaActualId = carga.senas.getOrNull(indiceActual)?.id.orEmpty(),
                soloEstaSena = ruta.soloEstaSena,
            ),
        )
    }

    /** Mientras no se haya avanzado, la sesión arranca en la seña que pidió la ruta. */
    private fun indiceResuelto(indice: Int, senas: List<Sena>): Int {
        val inicial = senas.indexOfFirst { it.id == ruta.senaInicial }.coerceAtLeast(0)
        val elegido = if (indice == INDICE_SIN_ELEGIR) inicial else indice
        return elegido.coerceIn(0, maxOf(0, senas.lastIndex))
    }

    private data class CargaDeSenas(
        val cargando: Boolean = true,
        val hayError: Boolean = false,
        val senas: List<Sena> = emptyList(),
    )

    private data class EstadoDeSesion(
        val indice: Int = INDICE_SIN_ELEGIR,
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
        val coincidencia: Float = 0f,
        val umbral: Float = 0f,
        val puntosDeMano: FloatArray = FloatArray(0),
        val proporcionDeImagen: Float = 0f,
    )

    companion object {
        /**
         * Suelo de lectura de la retroalimentación antes de reanudar sola. La pantalla parte de
         * aquí para preguntarle al sistema el tiempo recomendado, que puede ser mayor.
         */
        const val ESPERA_POR_DEFECTO_MS = 1_200L

        private const val ETIQUETA = "Practica"
        private const val INDICE_SIN_ELEGIR = -1
        private const val TIEMPO_DE_ESPERA_MS = 5_000L
    }
}

/** Traducción del resultado de dominio al estado que sabe dibujar la pantalla. */
private fun ResultadoIntento.aEstadoDeUi(): EstadoDeRetroalimentacion = when (this) {
    is ResultadoIntento.Acierto -> EstadoDeRetroalimentacion.Acertada(letra, confianza)
    is ResultadoIntento.OtraLetra -> EstadoDeRetroalimentacion.OtraLetra(letraDetectada)
    ResultadoIntento.NoReconocida -> EstadoDeRetroalimentacion.NoReconocida
    ResultadoIntento.SinMano -> EstadoDeRetroalimentacion.SinMano
}
