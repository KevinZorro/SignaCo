package com.ufps.signaco.core.notificaciones.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ufps.signaco.core.notificaciones.domain.ProgramadorDeRecordatorios
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Reprograma la alarma del recordatorio diario cuando el teléfono se reinicia
 * o se actualiza la aplicación.
 */
@AndroidEntryPoint
class ReceptorDeReinicio : BroadcastReceiver() {

    @Inject
    lateinit var programador: ProgramadorDeRecordatorios

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            programador.programarRecordatorioDiario()
        }
    }
}
