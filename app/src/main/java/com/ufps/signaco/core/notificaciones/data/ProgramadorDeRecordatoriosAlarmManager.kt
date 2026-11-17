package com.ufps.signaco.core.notificaciones.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ufps.signaco.core.notificaciones.domain.ProgramadorDeRecordatorios
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramadorDeRecordatoriosAlarmManager @Inject constructor(
    @ApplicationContext private val contexto: Context,
) : ProgramadorDeRecordatorios {

    private val alarmManager = contexto.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun programarRecordatorioDiario(hora: Int, minuto: Int) {
        if (alarmManager == null) return

        val intent = Intent(contexto, RecordatorioReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            contexto,
            CODIGO_ALARMA,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val ahora = Calendar.getInstance()
        val proximoDisparo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(ahora)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        CanalDeNotificaciones.crearCanalSiEsNecesario(contexto)

        // Usamos setInexactRepeating con INTERVAL_DAY para cumplir las directrices
        // de batería y Doze mode de Android Developers para recordatorios diarios.
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            proximoDisparo.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent,
        )
    }

    override fun cancelarRecordatorio() {
        if (alarmManager == null) return
        val intent = Intent(contexto, RecordatorioReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            contexto,
            CODIGO_ALARMA,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        const val CODIGO_ALARMA = 3001
    }
}
