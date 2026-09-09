package com.ufps.signaco.core.comun

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementacion real de [Reloj]. java.time esta disponible desde API 26, que es el minSdk.
 */
@Singleton
class RelojDelSistema @Inject constructor() : Reloj {
    override fun hoyEnDias(): Long = LocalDate.now().toEpochDay()
}
