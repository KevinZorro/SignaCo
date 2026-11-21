package com.ufps.signaco.core.notificaciones.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ufps.signaco.core.notificaciones.domain.SincronizarRecordatoriosUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reprograma la alarma del recordatorio diario cuando el teléfono se reinicia
 * o se actualiza la aplicación, si el usuario tiene la preferencia activa.
 */
@AndroidEntryPoint
class ReceptorDeReinicio : BroadcastReceiver() {

    @Inject
    lateinit var sincronizarRecordatorios: SincronizarRecordatoriosUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    sincronizarRecordatorios()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
