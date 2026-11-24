package com.ufps.signaco.feature.deletreo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.feature.deletreo.domain.EventoDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.MotorDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.usecase.CrearMotorDeDeletreoUseCase
import com.ufps.signaco.feature.deletreo.domain.usecase.ProcesarFrameDeDeletreoUseCase
import com.ufps.signaco.feature.practica.domain.model.FrameDeLandmarks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Solo traduce eventos del dominio a estado de pantalla: toda la lógica vive en
 * [MotorDeDeletreo] y los casos de uso.
 */
@HiltViewModel
class DeletreoViewModel @Inject constructor(
    private val crearMotor: CrearMotorDeDeletreoUseCase,
    private val procesarFrame: ProcesarFrameDeDeletreoUseCase,
    private val preferenciasRepository: PreferenciasRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeletreoUiState())
    val uiState: StateFlow<DeletreoUiState> = _uiState.asStateFlow()

    private var motor: MotorDeDeletreo? = null
    private val frameEnProceso = AtomicBoolean(false)

    init {
        viewModelScope.launch {
            preferenciasRepository.observarPreferencias().collect { preferencias ->
                _uiState.update {
                    it.copy(
                        reducirAnimaciones = preferencias.reducirAnimaciones,
                        retroalimentacionHaptica = preferencias.retroalimentacionHaptica,
                    )
                }
            }
        }
        viewModelScope.launch {
            runCatching { crearMotor() }
                .onSuccess { motor = it; _uiState.update { s -> s.copy(cargando = false) } }
                .onFailure { _uiState.update { s -> s.copy(cargando = false, error = true) } }
        }
    }

    fun alResponderElPermiso(concedido: Boolean) {
        _uiState.update { it.copy(permisoConcedido = concedido) }
    }

    fun informarErrorDeCamara() {
        _uiState.update { it.copy(error = true) }
    }

    fun alLlegarFrame(frame: FrameDeLandmarks) {
        val motor = motor ?: return
        if (!_uiState.value.capturando || _uiState.value.celebrando) return
        if (!frameEnProceso.compareAndSet(false, true)) return
        viewModelScope.launch {
            try {
                aplicar(procesarFrame(frame, motor), motor)
            } finally {
                frameEnProceso.set(false)
            }
        }
    }

    /** "Listo": cierra la palabra sin esperar la pausa. */
    /** "Terminar palabra": cierra la palabra en curso y celebra; al acabar el confeti se limpia. */
    fun terminarYCelebrar() {
        val motor = motor ?: return
        aplicar(motor.terminar(System.currentTimeMillis()), motor)
        _uiState.update { it.celebrar() }
    }

    /** Lo llama la pantalla cuando el confeti terminó: se borra lo deletreado. */
    fun alTerminarCelebracion() {
        motor?.reiniciar()
        _uiState.update { it.limpio() }
    }

    fun borrarTodo() {
        motor?.reiniciar()
        _uiState.update { it.limpio() }
    }

    fun alternarCaptura() {
        _uiState.update { it.copy(capturando = !it.capturando) }
    }

    private fun aplicar(eventos: List<EventoDeDeletreo>, motor: MotorDeDeletreo) {
        val sostenido = motor.progreso
        if (eventos.isEmpty()) {
            _uiState.update { it.copy(sostenido = sostenido) }
            return
        }
        _uiState.update { estado ->
            var palabras = estado.palabras
            for (e in eventos) {
                if (e is EventoDeDeletreo.BorrarPalabraSolicitado) palabras = palabras.dropLast(1)
                if (e is EventoDeDeletreo.PalabraTerminada) {
                    val r = e.resultado
                    palabras = palabras + PalabraDeletreada(
                        letras = e.letras.joinToString(" ") { if (it == "NN") "Ñ" else it },
                        sugerida = r.mejor,
                        otras = r.candidatas.drop(1).map { it.palabra },
                        ambigua = r.ambigua,
                    )
                }
            }
            estado.copy(
                letrasEnCurso = motor.letrasActuales.joinToString(" ") { if (it == "NN") "Ñ" else it },
                palabras = palabras,
                sostenido = sostenido,
            )
        }
    }
}
