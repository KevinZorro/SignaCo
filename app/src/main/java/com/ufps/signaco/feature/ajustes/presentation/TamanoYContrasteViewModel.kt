package com.ufps.signaco.feature.ajustes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.core.preferencias.domain.TamanoDeTexto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @param guardado la escala que ya está aplicada en la app. La vista previa la necesita para
 *        mostrar el borrador sin aplicar la escala dos veces.
 */
data class TamanoYContrasteUiState(
    val cargando: Boolean = true,
    val tamano: TamanoDeTexto = TamanoDeTexto.NORMAL,
    val altoContraste: Boolean = false,
    val reducirAnimaciones: Boolean = false,
    val guardado: TamanoDeTexto = TamanoDeTexto.NORMAL,
    val guardando: Boolean = false,
)

/**
 * A diferencia de Ajustes, aquí se edita un borrador: la persona ve el cambio en la vista
 * previa y lo confirma con «Guardar cambios».
 */
@HiltViewModel
class TamanoYContrasteViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TamanoYContrasteUiState())
    val uiState: StateFlow<TamanoYContrasteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val actuales = preferenciasRepository.observarPreferencias().first()
            _uiState.value = TamanoYContrasteUiState(
                cargando = false,
                tamano = actuales.tamanoDeTexto,
                altoContraste = actuales.altoContraste,
                reducirAnimaciones = actuales.reducirAnimaciones,
                guardado = actuales.tamanoDeTexto,
            )
        }
    }

    fun cambiarTamano(tamano: TamanoDeTexto) = _uiState.update { it.copy(tamano = tamano) }

    fun cambiarAltoContraste(activo: Boolean) = _uiState.update { it.copy(altoContraste = activo) }

    fun cambiarReducirAnimaciones(activo: Boolean) = _uiState.update { it.copy(reducirAnimaciones = activo) }

    fun guardar(alTerminar: () -> Unit) {
        val borrador = _uiState.value
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            preferenciasRepository.guardarApariencia(
                tamano = borrador.tamano,
                altoContraste = borrador.altoContraste,
                reducirAnimaciones = borrador.reducirAnimaciones,
            )
            alTerminar()
        }
    }
}
