package com.ufps.signaco

import android.app.Application
import com.ufps.signaco.core.notificaciones.domain.SincronizarRecordatoriosUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Punto de entrada de Hilt. Debe estar declarada en el manifiesto. */
@HiltAndroidApp
class SignaCoApplication : Application() {

    @Inject
    lateinit var sincronizarRecordatorios: SincronizarRecordatoriosUseCase

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Default).launch {
            sincronizarRecordatorios()
        }
    }
}
