package com.ufps.signaco.core.comun

/**
 * Fuente de tiempo del dominio. Existe para que la logica de racha sea comprobable
 * sin depender del reloj real del sistema.
 */
fun interface Reloj {
    /** Dia actual como numero de dias desde 1970-01-01. */
    fun hoyEnDias(): Long
}
