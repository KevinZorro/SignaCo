package com.ufps.signaco.feature.lecciones.presentation.detalle

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ufps.signaco.core.navegacion.Rutas
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerDetalleDeLeccionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetalleLeccionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    obtenerDetalle: ObtenerDetalleDeLeccionUseCase,
) : ViewModel() {

    /** Argumento de navegación con tipo: si la ruta cambia, esto deja de compilar. */
    val leccionId: String = savedStateHandle.toRoute<Rutas.DetalleLeccion>().leccionId

    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<DetalleLeccionUiState> = reintentos
        .flatMapLatest {
            obtenerDetalle(leccionId)
                .map { leccion -> DetalleLeccionUiState(cargando = false, leccion = leccion) }
                .onStart { emit(DetalleLeccionUiState(cargando = true)) }
                .catch { error ->
                    Log.e(ETIQUETA, "No se pudo cargar la lección $leccionId", error)
                    emit(DetalleLeccionUiState(cargando = false, hayError = true))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_DE_ESPERA_MS),
            initialValue = DetalleLeccionUiState(),
        )

    fun reintentar() {
        reintentos.update { it + 1 }
    }

    private companion object {
        const val ETIQUETA = "DetalleLeccion"
        const val TIEMPO_DE_ESPERA_MS = 5_000L
    }
}
