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
import com.ufps.signaco.feature.practica.domain.model.ResultadoIntento
import com.ufps.signaco.feature.practica.domain.model.TipoDeSimulacion
import com.ufps.signaco.feature.practica.domain.usecase.ObtenerSenasParaPracticarUseCase
import com.ufps.signaco.feature.practica.domain.usecase.SimularIntentoUseCase
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
import javax.inject.Inject

/**
 * La pantalla de práctica todavía no tiene cámara: los intentos se disparan a mano desde la
 * UI y pasan por [SimularIntentoUseCase]. Cuando entre CameraX, lo único que cambia es de
 * dónde salen los landmarks; el resto de este ViewModel se queda igual.
 *
 * No decide si el usuario acertó: eso lo resuelve el caso de uso y aquí solo se traduce a
 * un estado que la pantalla sabe dibujar.
 */
@HiltViewModel
class PracticaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    obtenerSenas: ObtenerSenasParaPracticarUseCase,
    private val preferenciasRepository: PreferenciasRepository,
    private val simularIntento: SimularIntentoUseCase,
) : ViewModel() {

    val leccionId: String = savedStateHandle.toRoute<Rutas.Practica>().leccionId

    private val estadoDeSesion = MutableStateFlow(EstadoDeSesion())

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

    /**
     * ANDAMIO TEMPORAL: fuerza un desenlace de reconocimiento. Se reemplaza por el flujo de
     * frames de la cámara.
     */
    fun simular(tipo: TipoDeSimulacion) {
        // Se manda el id de la seña, que es la etiqueta que entiende el clasificador.
        val senaObjetivo = uiState.value.senaActual?.id ?: return
        if (estadoDeSesion.value.evaluando) return

        estadoDeSesion.update {
            it.copy(evaluando = true, retroalimentacion = EstadoDeRetroalimentacion.Reconociendo)
        }
        viewModelScope.launch {
            val resultado = runCatching { simularIntento(senaObjetivo, tipo) }
                .onFailure { Log.e(ETIQUETA, "Falló el intento simulado", it) }
                .getOrNull()
            estadoDeSesion.update { sesion ->
                sesion.copy(
                    evaluando = false,
                    retroalimentacion = resultado?.aEstadoDeUi()
                        ?: EstadoDeRetroalimentacion.Esperando,
                    aciertos = sesion.aciertos +
                        if (resultado is ResultadoIntento.Acierto) 1 else 0,
                )
            }
        }
    }

    fun siguienteSena() {
        estadoDeSesion.update {
            it.copy(
                indice = it.indice + 1,
                retroalimentacion = EstadoDeRetroalimentacion.Esperando,
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
        evaluando = sesion.evaluando,
        retroalimentacion = sesion.retroalimentacion,
        aciertosDeLaSesion = sesion.aciertos,
        mostrarPistas = preferencias.mostrarPistas,
        retroalimentacionHaptica = preferencias.retroalimentacionHaptica,
    )

    private data class CargaDeSenas(
        val cargando: Boolean = true,
        val hayError: Boolean = false,
        val senas: List<Sena> = emptyList(),
    )

    private data class EstadoDeSesion(
        val indice: Int = 0,
        val evaluando: Boolean = false,
        val retroalimentacion: EstadoDeRetroalimentacion = EstadoDeRetroalimentacion.Esperando,
        val aciertos: Int = 0,
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
