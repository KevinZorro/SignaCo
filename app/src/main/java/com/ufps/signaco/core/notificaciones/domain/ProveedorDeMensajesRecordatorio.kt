package com.ufps.signaco.core.notificaciones.domain

import kotlin.random.Random

/**
 * Provee variaciones genéricas y amigables de mensajes de recordatorio para la tarde.
 */
interface ProveedorDeMensajesRecordatorio {
    fun obtenerMensaje(): MensajeDeRecordatorio
}

class ProveedorDeMensajesRecordatorioAleatorio(
    private val random: Random = Random.Default,
) : ProveedorDeMensajesRecordatorio {

    override fun obtenerMensaje(): MensajeDeRecordatorio {
        val indice = random.nextInt(MENSAJES.size)
        return MENSAJES[indice]
    }

    companion object {
        const val TITULO_POR_DEFECTO = "SignaCO"

        val MENSAJES: List<MensajeDeRecordatorio> = listOf(
            MensajeDeRecordatorio(
                titulo = TITULO_POR_DEFECTO,
                cuerpo = "¿Tienes 3 minutos libres? Recuerda practicar tus señas de hoy.",
            ),
            MensajeDeRecordatorio(
                titulo = TITULO_POR_DEFECTO,
                cuerpo = "Mantén tu racha activa: practica una lección de LSC en SignaCO.",
            ),
            MensajeDeRecordatorio(
                titulo = TITULO_POR_DEFECTO,
                cuerpo = "Un repaso rápido marca la diferencia. ¡Es hora de practicar señas!",
            ),
            MensajeDeRecordatorio(
                titulo = TITULO_POR_DEFECTO,
                cuerpo = "Aprender señas es constancia. Dedica un momento a tus señas de hoy.",
            ),
            MensajeDeRecordatorio(
                titulo = TITULO_POR_DEFECTO,
                cuerpo = "¡Hora de practicar! Entra a SignaCO y refuerza tu abecedario de señas.",
            ),
        )
    }
}
