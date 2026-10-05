package com.ufps.signaco.feature.progreso.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerCatalogoDeLeccionesUseCase
import com.ufps.signaco.feature.progreso.domain.usecase.BorrarProgresoUseCase
import com.ufps.signaco.feature.progreso.domain.usecase.ObtenerResumenDeProgresoUseCase
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgresoViewModel @Inject constructor(
    obtenerResumen: ObtenerResumenDeProgresoUseCase,
    obtenerCatalogo: ObtenerCatalogoDeLeccionesUseCase,
    private val borrarProgreso: BorrarProgresoUseCase,
) : ViewModel() {

    private val reintentos = MutableStateFlow(0)
    private val borrando = MutableStateFlow(false)

    val uiState: StateFlow<ProgresoUiState> = reintentos
        .flatMapLatest {
            combine(obtenerResumen(), obtenerCatalogo(), borrando) { resumen, categorias, enCurso ->
                ProgresoUiState(
                    cargando = false,
                    resumen = resumen,
                    lecciones = categorias.flatMap { it.lecciones },
                    borrando = enCurso,
                )
            }
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

    /** Solo se llama después de que la persona confirmó en el diálogo. */
    fun borrarProgreso() {
        viewModelScope.launch {
            borrando.value = true
            runCatching { borrarProgreso.invoke() }
                .onFailure { Log.e(ETIQUETA, "No se pudo borrar el progreso", it) }
            borrando.value = false
        }
    }

    private companion object {
        const val ETIQUETA = "Progreso"
        const val TIEMPO_DE_ESPERA_MS = 5_000L
    }
}
