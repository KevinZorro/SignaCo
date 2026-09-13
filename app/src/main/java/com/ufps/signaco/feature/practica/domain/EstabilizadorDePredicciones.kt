package com.ufps.signaco.feature.practica.domain

/**
 * Suavizado temporal (RF07): una seña solo se confirma cuando la misma predicción se mantiene
 * durante [k] frames consecutivos. Sin esto la pantalla parpadea entre letras.
 *
 * Es lógica de negocio y por eso vive en domain, no en el ViewModel ni en la capa de cámara:
 * el RNF19 del ERS nombra el suavizado temporal entre lo que debe tener pruebas unitarias.
 *
 * Tiene estado, así que se crea UNO POR SESIÓN de práctica y no se comparte. En el spike este
 * buffer vivía dentro del clasificador, que aquí es @Singleton; de haberlo dejado ahí, los
 * frames de una lección se habrían arrastrado a la siguiente.
 *
 * Confirma UNA SOLA VEZ por racha: mientras el usuario sostiene la misma pose, [registrar]
 * devuelve la clave en la transición y null después. Sin ese pestillo, a 30 fps se registraría
 * un intento nuevo cada k frames y el progreso del usuario quedaría inflado.
 */
class EstabilizadorDePredicciones(val k: Int) {

    init {
        require(k >= 1) { "k debe ser al menos 1 y llegó $k" }
    }

    private val ventana = ArrayDeque<String>()
    private var ultimaConfirmada: String? = null

    /** Frames consecutivos acumulados con la misma predicción, para dibujar el avance. */
    val avance: Int
        get() {
            val ultima = ventana.lastOrNull() ?: return 0
            return ventana.reversed().takeWhile { it == ultima }.size
        }

    /**
     * @return la clave recién confirmada, o null si todavía no hay [k] frames iguales o si esa
     *         misma clave ya se había confirmado en esta racha.
     */
    fun registrar(clave: String): String? {
        ventana.addLast(clave)
        while (ventana.size > k) ventana.removeFirst()

        val estable = ventana.size == k && ventana.all { it == ventana.first() }
        if (!estable) return null

        if (clave == ultimaConfirmada) return null
        ultimaConfirmada = clave
        return clave
    }

    /** Se llama cuando la mano sale de cuadro o al pasar a otra seña. */
    fun reiniciar() {
        ventana.clear()
        ultimaConfirmada = null
    }

    companion object {
        /** Clave con la que se estabiliza el "no reconocida", que también debe dejar de parpadear. */
        const val CLAVE_NO_RECONOCIDA = "__no_reconocida__"
    }
}
