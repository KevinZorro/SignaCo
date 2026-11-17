package com.ufps.signaco.core.notificaciones.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ufps.signaco.R

object CanalDeNotificaciones {
    const val CANAL_ID = "recordatorios_practica"

    fun crearCanalSiEsNecesario(contexto: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = contexto.getString(R.string.notificacion_canal_nombre)
            val descripcion = contexto.getString(R.string.notificacion_canal_descripcion)
            val importancia = NotificationManager.IMPORTANCE_DEFAULT

            val canal = NotificationChannel(CANAL_ID, nombre, importancia).apply {
                this.description = descripcion
            }

            val manager = contexto.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(canal)
        }
    }
}
