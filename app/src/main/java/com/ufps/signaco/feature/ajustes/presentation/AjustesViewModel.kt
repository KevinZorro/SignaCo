package com.ufps.signaco.feature.ajustes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufps.signaco.core.preferencias.domain.ManoDominante
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.core.preferencias.domain.PreferenciasUsuario
import com.ufps.signaco.core.preferencias.domain.VelocidadDeClips
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Los ajustes se guardan en el acto, sin botón de guardar (salvo tamaño y contraste). */
@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
) : ViewModel() {

    val preferencias: StateFlow<PreferenciasUsuario> = preferenciasRepository.observarPreferencias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), PreferenciasUsuario())

    fun cambiarManoDominante(mano: ManoDominante) {
        viewModelScope.launch { preferenciasRepository.cambiarManoDominante(mano) }
    }

    fun cambiarVelocidadDeClips(velocidad: VelocidadDeClips) {
        viewModelScope.launch { preferenciasRepository.cambiarVelocidadDeClips(velocidad) }
    }

    fun cambiarVibracion(activa: Boolean) {
        viewModelScope.launch { preferenciasRepository.cambiarRetroalimentacionHaptica(activa) }
    }

    fun cambiarSubtitulos(activos: Boolean) {
        viewModelScope.launch { preferenciasRepository.cambiarSubtitulosEnClips(activos) }
    }
}
