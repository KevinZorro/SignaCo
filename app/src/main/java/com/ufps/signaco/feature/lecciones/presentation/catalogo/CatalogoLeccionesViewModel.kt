package com.ufps.signaco.feature.lecciones.presentation.catalogo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerCatalogoDeLeccionesUseCase
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
import javax.inject.Inject

/**
 * No calcula nada: pide el catálogo ya cruzado con el progreso, y el resumen para el
 * encabezado (señas dominadas y racha), y los publica como estado. El cruce y el conteo
 * viven en los casos de uso.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CatalogoLeccionesViewModel @Inject constructor(
    obtenerCatalogo: ObtenerCatalogoDeLeccionesUseCase,
    obtenerResumen: ObtenerResumenDeProgresoUseCase,
) : ViewModel() {

    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<CatalogoLeccionesUiState> = reintentos
        .flatMapLatest {
            combine(obtenerCatalogo(), obtenerResumen()) { categorias, resumen ->
                CatalogoLeccionesUiState(
                    cargando = false,
                    categorias = categorias,
                    senasDominadas = resumen.senasDominadas,
                    senasDisponibles = resumen.senasDisponibles,
                    diasDeRacha = resumen.rachaActual,
                )
            }
                .onStart { emit(CatalogoLeccionesUiState(cargando = true)) }
                .catch { error ->
                    Log.e(ETIQUETA, "No se pudo cargar el catálogo", error)
                    emit(CatalogoLeccionesUiState(cargando = false, hayError = true))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_DE_ESPERA_MS),
            initialValue = CatalogoLeccionesUiState(),
        )

    fun reintentar() {
        reintentos.update { it + 1 }
    }

    private companion object {
        const val ETIQUETA = "CatalogoLecciones"
        const val TIEMPO_DE_ESPERA_MS = 5_000L
    }
}
