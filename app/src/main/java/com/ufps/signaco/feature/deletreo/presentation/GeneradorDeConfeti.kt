package com.ufps.signaco.feature.deletreo.presentation

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Una pieza de confeti. Todas las medidas son fracciones de la pantalla (0 a 1) para no depender
 * del tamaño: [xInicio] y [deriva] en horizontal, [velocidad] cuántas pantallas recorre.
 */
data class Particula(
    val xInicio: Float,
    val deriva: Float,
    /** Cuándo empieza a caer, como fracción de la animación. */
    val retraso: Float,
    val velocidad: Float,
    val giro: Float,
    val tamano: Float,
    /** Índice en la paleta que elige la pantalla (así los colores salen del tema). */
    val color: Int,
)

/**
 * Confeti determinista: la misma semilla da las mismas piezas, así se puede probar sin pantalla.
 * Solo cae y se desvanece fuera de cuadro: sin rebotes, como pide DESIGN.md.
 */
object GeneradorDeConfeti {

    fun generar(cantidad: Int, colores: Int, semilla: Int = 7): List<Particula> {
        val azar = Random(semilla)
        return List(cantidad) {
            Particula(
                xInicio = azar.nextFloat(),
                deriva = azar.nextFloat() * 0.3f - 0.15f,
                retraso = azar.nextFloat() * 0.35f,
                velocidad = 0.9f + azar.nextFloat() * 0.3f,
                giro = azar.nextFloat() * 2f * PI.toFloat(),
                tamano = 0.012f + azar.nextFloat() * 0.012f,
                color = azar.nextInt(colores),
            )
        }
    }

    /** Avance propio de la pieza (0 a 1) en el instante [t] de la animación. */
    private fun avance(p: Particula, t: Float): Float = ((t - p.retraso) / (1f - p.retraso)).coerceIn(0f, 1f)

    /** Posición vertical: arranca sobre el borde superior (< 0) y termina bajo el inferior (> 1). */
    fun y(p: Particula, t: Float): Float = -0.1f + avance(p, t) * p.velocidad * 1.3f

    fun x(p: Particula, t: Float): Float {
        val a = avance(p, t)
        return p.xInicio + p.deriva * a + sin(a * 6f * PI.toFloat() + p.giro) * 0.02f
    }

    /** Ángulo de la pieza en grados. */
    fun angulo(p: Particula, t: Float): Float = p.giro * 57.2958f + avance(p, t) * 540f
}
