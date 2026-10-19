package com.ufps.signaco.feature.quiz.presentation

import com.ufps.signaco.feature.practica.domain.model.EstadoDeReconocimiento
import com.ufps.signaco.feature.practica.presentation.EstadoDelPermiso
import com.ufps.signaco.feature.practica.presentation.ErrorDeReconocimiento
import com.ufps.signaco.feature.practica.presentation.camara.EstadoDelVisor
import com.ufps.signaco.feature.practica.presentation.camara.ResultadoDelIntento
import com.ufps.signaco.feature.quiz.domain.model.PreguntaDeCamara
import com.ufps.signaco.feature.quiz.domain.model.RespuestaCalificada

/** Cómo va la pregunta actual del quiz con cámara. */
sealed interface EstadoDePregunta {

    /** Todavía no hay intento confirmado, o se está intentando de nuevo. */
    data object Respondiendo : EstadoDePregunta

    /** Hizo la seña: la pregunta se ganó. */
    data class Acertada(val confianza: Float) : EstadoDePregunta

    /**
     * Falló el intento pero le quedan más. La captura vuelve a empezar sola tras la cuenta.
     *
     * @param letraVista la letra que vio el modelo, o null si no reconoció ninguna.
     */
    data class OtraOportunidad(val letraVista: String?, val intentosRestantes: Int) : EstadoDePregunta

    /** Gastó los intentos: la pregunta cuenta como error. */
    data class Fallada(val letraVista: String?) : EstadoDePregunta
}

data class QuizCamaraUiState(
    val cargando: Boolean = true,
    val hayError: Boolean = false,
    val preguntas: List<PreguntaDeCamara> = emptyList(),
    val indiceActual: Int = 0,
    /** Intentos confirmados en la pregunta actual. */
    val intentosEnPregunta: Int = 0,
    val intentosPorPregunta: Int = 0,
    val estadoDePregunta: EstadoDePregunta = EstadoDePregunta.Respondiendo,
    /** Una por pregunta ya resuelta, en orden. */
    val respuestas: List<RespuestaCalificada> = emptyList(),

    // --- Cámara y reconocimiento, igual que en la práctica ---
    val permiso: EstadoDelPermiso = EstadoDelPermiso.PENDIENTE,
    val capturando: Boolean = false,
    val camaraFrontal: Boolean = true,
    val cuentaRegresiva: Int? = null,
    val estadoDeReconocimiento: EstadoDeReconocimiento = EstadoDeReconocimiento.SIN_DETECCION,
    val noReconocida: Boolean = false,
    val coincidencia: Float = 0f,
    val umbral: Float = 0f,
    val puntosDeMano: FloatArray = FloatArray(0),
    val proporcionDeImagen: Float = 0f,
    val errorDelReconocimiento: ErrorDeReconocimiento? = null,
    val retroalimentacionHaptica: Boolean = true,
    val reducirAnimaciones: Boolean = false,
) {
    val preguntaActual: PreguntaDeCamara? get() = preguntas.getOrNull(indiceActual)

    val numeroDePregunta: Int get() = indiceActual + 1

    val total: Int get() = preguntas.size

    val esLaUltima: Boolean get() = preguntas.isNotEmpty() && indiceActual >= preguntas.lastIndex

    val sinPreguntas: Boolean get() = !cargando && !hayError && preguntas.isEmpty()

    /** El número del intento que se está haciendo, para «Intento 2 de 3». */
    val intentoActual: Int get() = (intentosEnPregunta + 1).coerceAtMost(intentosPorPregunta)

    /** La cuenta regresiva está en 3, 2 o 1. */
    val contando: Boolean get() = (cuentaRegresiva ?: 0) > 0

    /** Solo se analizan frames con permiso, captura activa, sin error y mientras se responde. */
    val procesandoFrames: Boolean
        get() = permiso == EstadoDelPermiso.CONCEDIDO && capturando &&
            estadoDePregunta == EstadoDePregunta.Respondiendo && errorDelReconocimiento == null

    /** La hoja de abajo y los colores del visor tras un intento. */
    val resultadoDelIntento: ResultadoDelIntento
        get() = when (estadoDePregunta) {
            EstadoDePregunta.Respondiendo -> ResultadoDelIntento.NINGUNO
            is EstadoDePregunta.Acertada -> ResultadoDelIntento.ACIERTO
            is EstadoDePregunta.OtraOportunidad, is EstadoDePregunta.Fallada -> ResultadoDelIntento.FALLO
        }

    val estadoDelVisor: EstadoDelVisor
        get() = EstadoDelVisor(
            puntosDeMano = puntosDeMano,
            proporcionDeImagen = proporcionDeImagen,
            capturando = capturando,
            estadoDeReconocimiento = estadoDeReconocimiento,
            noReconocida = noReconocida,
            coincidencia = coincidencia,
            umbral = umbral,
            cuentaRegresiva = cuentaRegresiva,
        )
}
