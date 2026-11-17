package com.ufps.signaco.core.notificaciones.domain

import com.ufps.signaco.core.comun.Reloj
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import com.ufps.signaco.feature.progreso.domain.RachaRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Resultado de evaluar si corresponde emitir la notificación hoy. */
sealed interface DecisionDeRecordatorio {
    data class Enviar(val mensaje: MensajeDeRecordatorio) : DecisionDeRecordatorio
    data class NoEnviar(val motivo: MotivoNoEnvio) : DecisionDeRecordatorio
}

enum class MotivoNoEnvio {
    RECORDATORIOS_DESACTIVADOS,
    YA_PRACTICO_HOY,
}

/**
 * QA y UX inteligente de recordatorios:
 * 1. Verifica si el usuario tiene activos los recordatorios en ajustes.
 * 2. Verifica si el usuario ya registró actividad hoy (para no molestarlo si ya practicó).
 * 3. Si procede, selecciona un mensaje rotativo.
 */
class EvaluarEnvioDeRecordatorioUseCase @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val rachaRepository: RachaRepository,
    private val reloj: Reloj,
    private val proveedorDeMensajes: ProveedorDeMensajesRecordatorio,
) {
    suspend operator fun invoke(): DecisionDeRecordatorio {
        val prefs = preferenciasRepository.observarPreferencias().first()
        if (!prefs.recordatoriosActivos) {
            return DecisionDeRecordatorio.NoEnviar(MotivoNoEnvio.RECORDATORIOS_DESACTIVADOS)
        }

        val hoy = reloj.hoyEnDias()
        val racha = rachaRepository.obtenerRacha()
        if (racha.ultimoDiaActivo == hoy) {
            return DecisionDeRecordatorio.NoEnviar(MotivoNoEnvio.YA_PRACTICO_HOY)
        }

        val mensaje = proveedorDeMensajes.obtenerMensaje()
        return DecisionDeRecordatorio.Enviar(mensaje)
    }
}
