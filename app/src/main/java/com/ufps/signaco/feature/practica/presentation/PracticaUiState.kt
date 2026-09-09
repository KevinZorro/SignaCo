package com.ufps.signaco.feature.practica.presentation

import com.ufps.signaco.core.senas.domain.model.Sena

/**
 * Lo que la pantalla de práctica le muestra al usuario tras el último intento.
 *
 * Son cinco estados y no dos: "no vemos tu mano" y "no reconocimos la seña" se corrigen de
 * formas distintas, así que se le dicen distinto.
 */
sealed interface EstadoDeRetroalimentacion {
    data object Esperando : EstadoDeRetroalimentacion
    data object Reconociendo : EstadoDeRetroalimentacion
    data class Acertada(val letra: String, val confianza: Float) : EstadoDeRetroalimentacion
    data class OtraLetra(val letraDetectada: String) : EstadoDeRetroalimentacion
    data object NoReconocida : EstadoDeRetroalimentacion
    data object SinMano : EstadoDeRetroalimentacion
}

data class PracticaUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val senas: List<Sena> = emptyList(),
    val indiceActual: Int = 0,
    val evaluando: Boolean = false,
    val retroalimentacion: EstadoDeRetroalimentacion = EstadoDeRetroalimentacion.Esperando,
    val aciertosDeLaSesion: Int = 0,
    val mostrarPistas: Boolean = true,
    val retroalimentacionHaptica: Boolean = true,
) {
    val senaActual: Sena? get() = senas.getOrNull(indiceActual)

    val esLaUltima: Boolean get() = senas.isNotEmpty() && indiceActual >= senas.lastIndex

    val sinSenas: Boolean get() = !cargando && !hayError && senas.isEmpty()
}
