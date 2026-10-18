package com.ufps.signaco.feature.practica.presentation

import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.feature.practica.domain.model.EstadoDeReconocimiento
import com.ufps.signaco.feature.practica.domain.model.PasoTrasElAcierto

/**
 * Lo que la pantalla de práctica le muestra al usuario tras el último intento.
 *
 * Son varios estados y no dos: "no vemos tu mano" y "no reconocimos la seña" se corrigen de
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

/** Qué puede fallar en el reconocimiento. La pantalla elige el texto; el ViewModel no los conoce. */
enum class ErrorDeReconocimiento {
    CONFIGURACION,
    MODELO,
    CAMARA,
}

/** Estado del permiso de cámara (RNF-13: nunca un cierre inesperado, siempre reintento). */
enum class EstadoDelPermiso {
    PENDIENTE,
    CONCEDIDO,
    DENEGADO,
}

data class PracticaUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val senas: List<Sena> = emptyList(),
    val indiceActual: Int = 0,
    val retroalimentacion: EstadoDeRetroalimentacion = EstadoDeRetroalimentacion.Esperando,
    val aciertosDeLaSesion: Int = 0,
    val mostrarPistas: Boolean = true,
    val retroalimentacionHaptica: Boolean = true,

    // --- Cámara y reconocimiento ---
    val permiso: EstadoDelPermiso = EstadoDelPermiso.PENDIENTE,
    /** RF10: la captura se inicia y se detiene con un control único. */
    val capturando: Boolean = false,
    /** RF04: se puede alternar entre cámara frontal y trasera. */
    val camaraFrontal: Boolean = true,
    /** RF09: buscando manos / reconociendo / sin detección. */
    val estadoDeReconocimiento: EstadoDeReconocimiento = EstadoDeReconocimiento.SIN_DETECCION,
    /** Frames consecutivos acumulados de los k que hacen falta, para dibujar el avance. */
    val avanceDeEstabilidad: Int = 0,
    val framesParaConfirmar: Int = 0,
    /** Mensaje de fallo de cámara o de carga del modelo. Accionable y con reintento. */
    val errorDelReconocimiento: ErrorDeReconocimiento? = null,
    /** Queda en true tras confirmar un intento, hasta que el usuario avance o reintente. */
    val intentoConfirmado: Boolean = false,
    /** Coincidencia del último frame con la seña objetivo, entre 0 y 1. */
    val coincidencia: Float = 0f,
    /** Umbral de la seña actual, de config_rechazo.json. */
    val umbral: Float = 0f,
    /** 21 puntos (x, y) sobre la imagen de la cámara, para dibujar la mano. Vacío si no hay mano. */
    val puntosDeMano: FloatArray = FloatArray(0),
    val proporcionDeImagen: Float = 0f,
    val reducirAnimaciones: Boolean = false,
    /**
     * Segundos que faltan para que el intento empiece solo (3, 2, 1), 0 justo al empezar y null
     * si no hay cuenta en pantalla.
     */
    val cuentaRegresiva: Int? = null,

    /** True si se practica una letra suelta elegida de la lista de la lección. */
    val soloEstaSena: Boolean = false,
    /** A dónde lleva el botón que aparece tras acertar. Lo decide el dominio. */
    val pasoTrasElAcierto: PasoTrasElAcierto = PasoTrasElAcierto.TerminarLeccion,
) {
    val senaActual: Sena? get() = senas.getOrNull(indiceActual)

    val esLaUltima: Boolean get() = senas.isNotEmpty() && indiceActual >= senas.lastIndex

    /** La cuenta regresiva está en 3, 2 o 1: el intento todavía no empezó. */
    val contando: Boolean get() = (cuentaRegresiva ?: 0) > 0

    val sinSenas: Boolean get() = !cargando && !hayError && senas.isEmpty()

    /** Solo se procesan frames si hay permiso, la captura está activa y no hay intento resuelto. */
    val procesandoFrames: Boolean
        get() = permiso == EstadoDelPermiso.CONCEDIDO && capturando && !intentoConfirmado &&
            errorDelReconocimiento == null
}
