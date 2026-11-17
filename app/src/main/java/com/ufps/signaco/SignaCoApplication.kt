package com.ufps.signaco

import android.app.Application
import com.ufps.signaco.core.notificaciones.domain.ProgramadorDeRecordatorios
import com.ufps.signaco.core.preferencias.domain.PreferenciasRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Punto de entrada de Hilt. Debe estar declarada en el manifiesto. */
@HiltAndroidApp
class SignaCoApplication : Application() {

    @Inject
    lateinit var programadorDeRecordatorios: ProgramadorDeRecordatorios

    @Inject
    lateinit var preferenciasRepository: PreferenciasRepository

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Default).launch {
            val preferencias = preferenciasRepository.observarPreferencias().first()
            if (preferencias.recordatoriosActivos) {
                programadorDeRecordatorios.programarRecordatorioDiario()
            }
        }
    }
}
