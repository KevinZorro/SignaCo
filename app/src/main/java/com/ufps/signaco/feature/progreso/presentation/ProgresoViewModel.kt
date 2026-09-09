package com.ufps.signaco.feature.progreso.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufps.signaco.feature.progreso.domain.usecase.ObtenerResumenDeProgresoUseCase
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
class ProgresoViewModel @Inject constructor(
    obtenerResumen: ObtenerResumenDeProgresoUseCase,
) : ViewModel() {

    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<ProgresoUiState> = reintentos
        .flatMapLatest {
            obtenerResumen()
                .map { resumen -> ProgresoUiState(cargando = false, resumen = resumen) }
                .onStart { emit(ProgresoUiState(cargando = true)) }
                .catch { error ->
                    Log.e(ETIQUETA, "No se pudo cargar el progreso", error)
                    emit(ProgresoUiState(cargando = false, hayError = true))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_DE_ESPERA_MS),
            initialValue = ProgresoUiState(),
        )

    fun reintentar() {
        reintentos.update { it + 1 }
    }

    private companion object {
        const val ETIQUETA = "Progreso"
        const val TIEMPO_DE_ESPERA_MS = 5_000L
    }
}
