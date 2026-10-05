package com.ufps.signaco.core.navegacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.core.preferencias.domain.PreferenciasUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Lo que necesita el armazón antes de dibujar: el tamaño de texto y el contraste para el tema,
 * y si ya se pasó por el primer uso para elegir el destino inicial. null mientras se lee.
 */
@HiltViewModel
class SignaCoAppViewModel @Inject constructor(
    preferenciasRepository: PreferenciasRepository,
) : ViewModel() {

    val preferencias: StateFlow<PreferenciasUsuario?> = preferenciasRepository.observarPreferencias()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
