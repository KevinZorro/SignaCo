package com.ufps.signaco.feature.diccionario.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.feature.diccionario.domain.usecase.BuscarEnDiccionarioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * El filtrado no se hace aquí: se le pide al caso de uso y esta clase solo guarda el texto
 * que escribió el usuario.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiccionarioViewModel @Inject constructor(
    buscarEnDiccionario: BuscarEnDiccionarioUseCase,
) : ViewModel() {

    private val consulta = MutableStateFlow("")
    private val categoria = MutableStateFlow<Categoria?>(null)
    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<DiccionarioUiState> =
        combine(consulta, categoria, reintentos) { texto, filtro, _ -> texto to filtro }
            .flatMapLatest { (texto, filtro) ->
                buscarEnDiccionario(texto, filtro)
                    .map { senas ->
                        DiccionarioUiState(cargando = false, consulta = texto, categoria = filtro, senas = senas)
                    }
                    .onStart { emit(DiccionarioUiState(cargando = true, consulta = texto, categoria = filtro)) }
                    .catch { error ->
                        Log.e(ETIQUETA, "No se pudo cargar el diccionario", error)
                        emit(
                            DiccionarioUiState(
                                cargando = false,
                                hayError = true,
                                consulta = texto,
                                categoria = filtro,
                            ),
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIEMPO_DE_ESPERA_MS),
                initialValue = DiccionarioUiState(),
            )

    fun cambiarConsulta(texto: String) {
        consulta.value = texto
    }

    fun cambiarCategoria(nueva: Categoria?) {
        categoria.value = nueva
    }

    fun reintentar() {
        reintentos.update { it + 1 }
    }

    private companion object {
        const val ETIQUETA = "Diccionario"
        const val TIEMPO_DE_ESPERA_MS = 5_000L
    }
}
