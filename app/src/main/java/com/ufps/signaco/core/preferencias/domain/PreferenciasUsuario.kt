package com.ufps.signaco.core.preferencias.domain

/**
 * Ajustes del usuario. Van en DataStore, no en Room: son preferencias, no datos.
 *
 * @param mostrarPistas si la pantalla de practica muestra la descripcion de la seña.
 * @param retroalimentacionHaptica vibrar al acertar. RNF-09: toda señal sonora tiene que
 *        tener equivalente visual o háptico, porque parte de los usuarios son personas sordas.
 */
data class PreferenciasUsuario(
    val mostrarPistas: Boolean = true,
    val retroalimentacionHaptica: Boolean = true,
)
