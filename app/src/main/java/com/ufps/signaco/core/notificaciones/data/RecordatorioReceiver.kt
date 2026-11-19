package com.ufps.signaco.core.notificaciones.data

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ufps.signaco.MainActivity
import com.ufps.signaco.R
import com.ufps.signaco.core.notificaciones.domain.DecisionDeRecordatorio
import com.ufps.signaco.core.notificaciones.domain.EvaluarEnvioDeRecordatorioUseCase
import com.ufps.signaco.core.notificaciones.domain.SincronizarRecordatoriosUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receptor de la alarma de recordatorio diario.
 * Evalúa si el usuario ya practicó o si tiene los recordatorios activos antes de emitir,
 * y sincroniza la siguiente alarma según las preferencias.
 */
@AndroidEntryPoint
class RecordatorioReceiver : BroadcastReceiver() {

    @Inject
    lateinit var evaluarEnvio: EvaluarEnvioDeRecordatorioUseCase

    @Inject
    lateinit var sincronizarRecordatorios: SincronizarRecordatoriosUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (val decision = evaluarEnvio()) {
                    is DecisionDeRecordatorio.Enviar -> {
                        val cuerpoResId = when (decision.indiceMensaje) {
                            0 -> R.string.recordatorio_mensaje_1
                            1 -> R.string.recordatorio_mensaje_2
                            2 -> R.string.recordatorio_mensaje_3
                            3 -> R.string.recordatorio_mensaje_4
                            else -> R.string.recordatorio_mensaje_5
                        }
                        val cuerpo = context.getString(cuerpoResId)
                        val titulo = context.getString(R.string.app_name)
                        mostrarNotificacion(context, titulo, cuerpo)
                    }
                    is DecisionDeRecordatorio.NoEnviar -> {
                        // No se envía (ya practicó hoy o están desactivados)
                    }
                }
                sincronizarRecordatorios()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun mostrarNotificacion(context: Context, titulo: String, cuerpo: String) {
        CanalDeNotificaciones.crearCanalSiEsNecesario(context)

        // Verificación de permiso en Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val tienePermiso = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!tienePermiso) return
        }

        val intentDestino = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            CODIGO_PENDING_INTENT,
            intentDestino,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificacion = NotificationCompat.Builder(context, CanalDeNotificaciones.CANAL_ID)
            .setSmallIcon(R.drawable.ic_circle)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(ID_NOTIFICACION, notificacion)
        } catch (_: SecurityException) {
            // Protección ante denegación concurrente de permisos
        }
    }

    companion object {
        const val ID_NOTIFICACION = 1001
        const val CODIGO_PENDING_INTENT = 2001
    }
}
