package com.ufps.signaco.feature.practica.domain

/**
 * Suavizado temporal (RF07): una seña solo se confirma cuando la misma predicción se mantiene
 * durante [k] frames consecutivos y, opcionalmente, durante una ventana temporal mínima
 * [duracionMinimaMs] (por defecto 1 segundo en la práctica real para evitar falsos positivos
 * transitorios mientras se acomoda la mano).
 *
 * Además, calcula la [coincidenciaPromedio] de las muestras recientes en una ventana de hasta 1 s
 * para que el indicador visual en pantalla no parpadee ni salte con picos de frames aislados.
 *
 * Es lógica de negocio y por eso vive en domain, no en el ViewModel ni en la capa de cámara:
 * el RNF19 del ERS nombra el suavizado temporal entre lo que debe tener pruebas unitarias.
 *
 * Tiene estado, así que se crea UNO POR SESIÓN de práctica y no se comparte.
 *
 * Confirma UNA SOLA VEZ por racha: mientras el usuario sostiene la misma pose, [registrar]
 * devuelve la clave en la transición y null después. Sin ese pestillo, a 30 fps se registraría
 * un intento nuevo repetidamente y el progreso del usuario quedaría inflado.
 */
class EstabilizadorDePredicciones(
    val k: Int,
    val duracionMinimaMs: Long = 0L,
) {

    init {
        require(k >= 1) { "k debe ser al menos 1 y llegó $k" }
        require(duracionMinimaMs >= 0L) { "duracionMinimaMs no puede ser negativa y llegó $duracionMinimaMs" }
    }

    private data class Muestra(
        val clave: String,
        val coincidencia: Float,
        val timestampMs: Long,
    )

    private val muestras = ArrayDeque<Muestra>()
    private var ultimaConfirmada: String? = null
    private var tiempoInicioRacha: Long? = null

    /** Frames consecutivos acumulados con la misma predicción, para dibujar el avance. */
    val avance: Int
        get() {
            val ultima = muestras.lastOrNull()?.clave ?: return 0
            return muestras.reversed().takeWhile { it.clave == ultima }.size
        }

    /** Tiempo en ms acumulado sosteniendo la misma predicción en la racha actual. */
    val duracionRachaMs: Long
        get() {
            val inicio = tiempoInicioRacha ?: return 0L
            val ultimo = muestras.lastOrNull()?.timestampMs ?: return 0L
            return (ultimo - inicio).coerceAtLeast(0L)
        }

    /**
     * Promedio de coincidencia de las muestras en la ventana reciente (hasta 1.000 ms o k frames).
     * Evita que la barra y el porcentaje de la UI salten con el ruido de un solo frame.
     */
    val coincidenciaPromedio: Float
        get() {
            if (muestras.isEmpty()) return 0f
            var suma = 0f
            for (m in muestras) {
                suma += m.coincidencia
            }
            return (suma / muestras.size).coerceIn(0f, 1f)
        }

    /**
     * Registra una predicción con su coincidencia y timestamp.
     *
     * @param clave letra reconocida o [CLAVE_NO_RECONOCIDA].
     * @param coincidencia confianza o coincidencia de la seña en este frame.
     * @param timestampMs marca de tiempo del frame.
     * @return la clave recién confirmada, o null si todavía no se cumplen los criterios de
     *         estabilidad o si esa misma clave ya se había confirmado en esta racha.
     */
    fun registrar(
        clave: String,
        coincidencia: Float = 0f,
        timestampMs: Long = 0L,
    ): String? {
        val ultimaClave = muestras.lastOrNull()?.clave

        if (clave != ultimaClave) {
            tiempoInicioRacha = timestampMs
        } else if (tiempoInicioRacha == null) {
            tiempoInicioRacha = timestampMs
        }

        muestras.addLast(Muestra(clave, coincidencia, timestampMs))

        // Si se especificó ventana de tiempo, conservamos muestras dentro del rango de 1 s
        if (duracionMinimaMs > 0L) {
            val limiteInferior = timestampMs - maxOf(duracionMinimaMs, VENTANA_PROMEDIO_MAX_MS)
            while (muestras.size > 1 && muestras.first().timestampMs < limiteInferior) {
                muestras.removeFirst()
            }
        } else {
            while (muestras.size > k) {
                muestras.removeFirst()
            }
        }

        // Criterio 1: mínimo k frames consecutivos iguales
        val framesConsecutivos = avance
        val tieneSuficientesFrames = framesConsecutivos >= k

        // Criterio 2: si hay duracionMinimaMs, la racha debe durar al menos ese tiempo
        val tiempoTranscurrido = if (tiempoInicioRacha != null) timestampMs - tiempoInicioRacha!! else 0L
        val tieneSuficienteTiempo = duracionMinimaMs <= 0L || tiempoTranscurrido >= duracionMinimaMs

        val estable = tieneSuficientesFrames && tieneSuficienteTiempo
        if (!estable) return null

        if (clave == ultimaConfirmada) return null
        ultimaConfirmada = clave
        return clave
    }

    /** Se llama cuando la mano sale de cuadro o al pasar a otra seña. */
    fun reiniciar() {
        muestras.clear()
        ultimaConfirmada = null
        tiempoInicioRacha = null
    }

    companion object {
        /** Clave con la que se estabiliza el "no reconocida", que también debe dejar de parpadear. */
        const val CLAVE_NO_RECONOCIDA = "__no_reconocida__"
        const val VENTANA_PROMEDIO_MAX_MS = 1_000L
    }
}
