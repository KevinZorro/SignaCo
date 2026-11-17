package com.ufps.signaco.core.notificaciones.domain

/**
 * Contenido textual de una notificación de recordatorio.
 */
data class MensajeDeRecordatorio(
    val titulo: String,
    val cuerpo: String,
)
