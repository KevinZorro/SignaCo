package com.ufps.signaco.feature.lecciones.presentation.primeruso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufps.signaco.core.preferencias.domain.ManoDominante
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConoceTuManoViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
) : ViewModel() {

    /** El esquema se espeja si la persona eligió la mano izquierda. */
    val manoIzquierda: StateFlow<Boolean> = preferenciasRepository.observarPreferencias()
        .map { it.manoDominante == ManoDominante.IZQUIERDA }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    fun continuar(alTerminar: () -> Unit) {
        viewModelScope.launch {
            preferenciasRepository.marcarPrimerUsoCompletado()
            alTerminar()
        }
    }
}
