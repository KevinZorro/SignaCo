package com.ufps.signaco.core.notificaciones.domain

/**
 * Contrato para programar o cancelar el recordatorio diario local.
 */
interface ProgramadorDeRecordatorios {
    fun programarRecordatorioDiario(hora: Int = HORA_POR_DEFECTO, minuto: Int = MINUTO_POR_DEFECTO)
    fun cancelarRecordatorio()

    companion object {
        const val HORA_POR_DEFECTO = 18 // 6:00 PM (en la tarde)
        const val MINUTO_POR_DEFECTO = 0
    }
}
