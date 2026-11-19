package com.ufps.signaco.feature.practica.domain

/**
 * Suavizado temporal (RF07): una seña solo se confirma cuando se mantiene estable durante
 * una ventana temporal [ventanaMs] (por defecto 1.000 ms) mediante votación por mayoría
 * (proporción ≥ [fraccionMinima]), o durante [k] frames consecutivos si [ventanaMs] es 0.
 *
 * Además, calcula la [coincidenciaPromedio] de las muestras vigentes en la ventana
 * para que el indicador visual en pantalla no parpadee ni salte con picos de frames aislados.
 *
 * Es lógica de negocio y por eso vive en domain, no en el ViewModel ni en la capa de cámara:
 * el RNF19 del ERS nombra el suavizado temporal entre lo que debe tener pruebas unitarias.
 *
 * Tiene estado, así que se crea UNO POR SESIÓN de práctica y no se comparte.
 *
 * Confirma UNA SOLA VEZ por sostén (pestillo): mientras el usuario sostiene la misma pose,
 * [registrar] devuelve la clave en la transición y null después.
 */
class EstabilizadorDePredicciones(
    val k: Int,
    val ventanaMs: Long = 0L,
    val fraccionMinima: Float = 0.8f,
) {

    init {
        require(k >= 1) { "k debe ser al menos 1 y llegó $k" }
        require(ventanaMs >= 0L) { "ventanaMs no puede ser negativa y llegó $ventanaMs" }
        if (ventanaMs > 0L) {
            require(fraccionMinima > 0.5f && fraccionMinima <= 1.0f) {
                "fraccionMinima debe estar en (0.5, 1.0] y llegó $fraccionMinima"
            }
        }
    }

    private data class Muestra(
        val clave: String,
        val coincidencia: Float,
        val timestampMs: Long,
    )

    private val muestras = ArrayDeque<Muestra>()
    private var ultimaConfirmada: String? = null
    private var timestampPrimeraMuestra: Long? = null
    private var ultimoTimestamp: Long? = null

    /** Frames consecutivos acumulados con la misma predicción al final de la ventana. */
    val avance: Int
        get() {
            val ultima = muestras.lastOrNull()?.clave ?: return 0
            return muestras.reversed().takeWhile { it.clave == ultima }.size
        }

    /**
     * Promedio de coincidencia de las muestras vigentes en la ventana.
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
     *         estabilidad o si esa misma clave ya se había confirmado en este sostén.
     */
    fun registrar(
        clave: String,
        coincidencia: Float = 0f,
        timestampMs: Long = 0L,
    ): String? {
        if (ventanaMs == 0L) {
            muestras.addLast(Muestra(clave, coincidencia, timestampMs))
            while (muestras.size > k) {
                muestras.removeFirst()
            }
            val estable = muestras.size == k && muestras.all { it.clave == muestras.first().clave }
            if (!estable) return null

            if (clave == ultimaConfirmada) return null
            ultimaConfirmada = clave
            return clave
        }

        // Modo ventana temporal con votación por mayoría
        if (ultimoTimestamp != null) {
            val salto = timestampMs - ultimoTimestamp!!
            if (salto > ventanaMs || timestampMs < ultimoTimestamp!!) {
                reiniciar()
            }
        }

        if (timestampPrimeraMuestra == null) {
            timestampPrimeraMuestra = timestampMs
        }
        ultimoTimestamp = timestampMs

        muestras.addLast(Muestra(clave, coincidencia, timestampMs))

        val limiteInferior = timestampMs - ventanaMs
        while (muestras.isNotEmpty() && muestras.first().timestampMs < limiteInferior) {
            muestras.removeFirst()
        }

        val tiempoTranscurrido = timestampMs - (timestampPrimeraMuestra ?: timestampMs)
        if (tiempoTranscurrido < ventanaMs || muestras.size < k) {
            return null
        }

        val conteos = mutableMapOf<String, Int>()
        for (m in muestras) {
            conteos[m.clave] = (conteos[m.clave] ?: 0) + 1
        }
        val mayoritaria = conteos.maxByOrNull { it.value } ?: return null
        val fraccion = mayoritaria.value.toFloat() / muestras.size

        if (fraccion < fraccionMinima) {
            return null
        }

        val claveGanadora = mayoritaria.key
        if (claveGanadora == ultimaConfirmada) {
            return null
        }

        ultimaConfirmada = claveGanadora
        return claveGanadora
    }

    /** Se llama cuando la mano sale de cuadro o al pasar a otra seña. */
    fun reiniciar() {
        muestras.clear()
        ultimaConfirmada = null
        timestampPrimeraMuestra = null
        ultimoTimestamp = null
    }

    companion object {
        /** Clave con la que se estabiliza el "no reconocida", que también debe dejar de parpadear. */
        const val CLAVE_NO_RECONOCIDA = "__no_reconocida__"
    }
}
