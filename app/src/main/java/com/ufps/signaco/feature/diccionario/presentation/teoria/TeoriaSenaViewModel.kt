package com.ufps.signaco.feature.diccionario.presentation.teoria

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ufps.signaco.core.navegacion.Rutas
import com.ufps.signaco.core.senas.domain.SenaRepository
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerLeccionDeSenaUseCase
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

/** Una seña del diccionario y, si se practica en alguna lección, cuál es. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeoriaSenaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    senaRepository: SenaRepository,
    obtenerLeccionDeSena: ObtenerLeccionDeSenaUseCase,
) : ViewModel() {

    val senaId: String = savedStateHandle.toRoute<Rutas.TeoriaSena>().senaId

    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<TeoriaSenaUiState> = reintentos
        .flatMapLatest {
            combine(senaRepository.observarSena(senaId), obtenerLeccionDeSena(senaId)) { sena, leccionId ->
                TeoriaSenaUiState(cargando = false, sena = sena, leccionId = leccionId)
            }
                .onStart { emit(TeoriaSenaUiState(cargando = true)) }
                .catch { error ->
                    Log.e(ETIQUETA, "No se pudo cargar la seña $senaId", error)
                    emit(TeoriaSenaUiState(cargando = false, hayError = true))
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIEMPO_DE_ESPERA_MS), TeoriaSenaUiState())

    fun reintentar() {
        reintentos.update { it + 1 }
    }

    private companion object {
        const val ETIQUETA = "TeoriaSena"
        const val TIEMPO_DE_ESPERA_MS = 5_000L
    }
}
