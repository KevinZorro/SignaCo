package com.ufps.signaco.feature.deletreo

import com.ufps.signaco.feature.deletreo.domain.DetectorDeGestos
import com.ufps.signaco.feature.deletreo.domain.Gestos
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Manos sintéticas, las mismas de tests/test_gestures.py del repo del modelo. */
class DetectorDeGestosTest {

    private val nombres = listOf("thumb", "index", "middle", "ring", "pinky")
    private val base = mapOf(
        "thumb" to (-0.22f to 0.18f), "index" to (-0.16f to 0.50f), "middle" to (0.0f to 0.55f),
        "ring" to (0.16f to 0.50f), "pinky" to (0.30f to 0.42f),
    )
    private val primero = mapOf("thumb" to 1, "index" to 5, "middle" to 9, "ring" to 13, "pinky" to 17)

    private fun mano(ext: Map<String, Boolean>, fan: Float = 0.35f): FloatArray {
        val p = Array(21) { FloatArray(3) }
        nombres.forEachIndexed { k, n ->
            val (bx, by) = base.getValue(n)
            val i0 = primero.getValue(n)
            p[i0] = floatArrayOf(bx, by, 0f)
            val extendido = ext.getValue(n)
            var ang = (k - 2) * fan * (if (n == "thumb") 1.6f else 1f)
            var dx = sin(ang)
            var dy = cos(ang)
            if (n == "thumb") {
                ang = if (extendido) -1.2f else -0.2f
                dx = -abs(sin(ang))
                dy = cos(ang) * 0.4f + 0.2f
            }
            for (s in 1..3) {
                if (extendido) {
                    p[i0 + s] = floatArrayOf(bx + dx * 0.18f * s, by + dy * 0.18f * s, 0f)
                } else {
                    val atras = if (s == 1) 0.12f else if (s == 2) 0.05f else -0.02f
                    p[i0 + s] = floatArrayOf(bx * 0.8f + dx * atras, by - 0.05f * s + dy * atras, 0.02f * s)
                }
            }
        }
        var maxNorma = 1e-6f
        for (q in p) maxNorma = max(maxNorma, sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2]))
        return FloatArray(63) { p[it / 3][it % 3] / maxNorma }
    }

    private val todos = nombres.associateWith { true }
    private val puno = nombres.associateWith { false }
    private val detector = DetectorDeGestos()

    @Test fun `la mano abierta borra`() = assertEquals(Gestos.BORRAR, detector.detectar(mano(todos)))

    @Test fun `te quiero da espacio`() =
        assertEquals(Gestos.ESPACIO, detector.detectar(mano(todos + mapOf("middle" to false, "ring" to false))))

    @Test fun `el puño y señalar no son gestos`() {
        assertNull(detector.detectar(mano(puno)))
        assertNull(detector.detectar(mano(puno + ("index" to true))))
    }

    @Test fun `Y y L no son espacio`() {
        assertNull(detector.detectar(mano(puno + mapOf("thumb" to true, "pinky" to true))))
        assertNull(detector.detectar(mano(puno + mapOf("thumb" to true, "index" to true))))
    }

    @Test fun `la B con dedos juntos no es borrar`() {
        assertNull(detector.detectar(mano(todos + ("thumb" to false), fan = 0f)))
    }

    @Test fun `un arreglo de otro tamaño no es gesto`() = assertNull(detector.detectar(FloatArray(10)))
}
