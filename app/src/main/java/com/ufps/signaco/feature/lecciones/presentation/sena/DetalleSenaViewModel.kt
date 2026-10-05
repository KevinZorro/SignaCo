package com.ufps.signaco.feature.lecciones.presentation.sena

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ufps.signaco.core.navegacion.Rutas
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerDetalleDeLeccionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Una seña de la lección antes de la cámara: se mira la indicación y se imita. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetalleSenaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    obtenerDetalle: ObtenerDetalleDeLeccionUseCase,
    preferenciasRepository: PreferenciasRepository,
) : ViewModel() {

    private val ruta = savedStateHandle.toRoute<Rutas.DetalleSena>()
    val leccionId: String = ruta.leccionId
    val senaId: String = ruta.senaId

    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<DetalleSenaUiState> = reintentos
        .flatMapLatest {
            combine(
                obtenerDetalle(leccionId),
                preferenciasRepository.observarPreferencias(),
            ) { leccion, preferencias ->
                DetalleSenaUiState(
                    cargando = false,
                    tituloDeLeccion = leccion?.leccion?.titulo.orEmpty(),
                    sena = leccion?.senas?.firstOrNull { it.sena.id == senaId }?.sena,
                    subtitulos = preferencias.subtitulosEnClips,
                )
            }
                .onStart { emit(DetalleSenaUiState(cargando = true)) }
                .catch { error ->
                    Log.e(ETIQUETA, "No se pudo cargar la seña $senaId", error)
                    emit(DetalleSenaUiState(cargando = false, hayError = true))
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIEMPO_DE_ESPERA_MS), DetalleSenaUiState())

    fun reintentar() {
        reintentos.update { it + 1 }
    }

    private companion object {
        const val ETIQUETA = "DetalleSena"
        const val TIEMPO_DE_ESPERA_MS = 5_000L
    }
}
