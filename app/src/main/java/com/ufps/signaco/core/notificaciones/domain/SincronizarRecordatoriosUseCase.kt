package com.ufps.signaco.core.notificaciones.domain

import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Sincroniza el estado de la alarma de recordatorio con la preferencia del usuario:
 * si están activos, programa la alarma diaria (alrededor de las 6 p.m.); si no, la cancela.
 */
class SincronizarRecordatoriosUseCase @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val programador: ProgramadorDeRecordatorios,
) {
    suspend operator fun invoke() {
        val prefs = preferenciasRepository.observarPreferencias().first()
        if (prefs.recordatoriosActivos) {
            programador.programarRecordatorioDiario()
        } else {
            programador.cancelarRecordatorio()
        }
    }
}
