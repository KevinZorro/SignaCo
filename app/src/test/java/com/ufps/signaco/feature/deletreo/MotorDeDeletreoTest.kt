package com.ufps.signaco.feature.deletreo

import com.ufps.signaco.feature.deletreo.domain.ConfiguracionDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.DecodificadorDePalabras
import com.ufps.signaco.feature.deletreo.domain.EventoDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.FrameDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.LetraObservada
import com.ufps.signaco.feature.deletreo.domain.MotorDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.Palabras
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mismos casos que tests/test_spelling.py del repo del modelo: si uno cambia, cambia el otro. */
class MotorDeDeletreoTest {

    private val config = ConfiguracionDeDeletreo()
    private val lista = Palabras.cargar(
        File("src/main/assets/palabras_deletreo.txt").readLines(),
        config.letrasExcluidas,
    )

    private fun motor() = MotorDeDeletreo(config, DecodificadorDePalabras(lista, config))

    private sealed interface Seg
    private data class Sost(val letra: String, val ms: Long, val conf: Float = 0.9f) : Seg
    private data class Hueco(val ms: Long, val sinMano: Boolean = true) : Seg

    private fun correr(vararg segs: Seg): List<EventoDeDeletreo> {
        val m = motor()
        val eventos = ArrayList<EventoDeDeletreo>()
        var t = 0L
        for (s in segs) {
            when (s) {
                is Sost -> {
                    val n = s.ms / PASO
                    for (i in 0..n) eventos += m.registrar(FrameDeDeletreo.Letra(t + i * PASO, s.letra, s.conf))
                    t += n * PASO
                }
                is Hueco -> {
                    val n = s.ms / PASO
                    for (i in 1..n) {
                        val f = if (s.sinMano) {
                            FrameDeDeletreo.SinMano(t + i * PASO)
                        } else {
                            FrameDeDeletreo.Rechazada(t + i * PASO)
                        }
                        eventos += m.registrar(f)
                    }
                    t += n * PASO
                }
            }
        }
        return eventos
    }

    private fun letras(e: List<EventoDeDeletreo>) =
        e.filterIsInstance<EventoDeDeletreo.LetraConfirmada>().map { it.letra }

    private fun palabras(e: List<EventoDeDeletreo>) = e.filterIsInstance<EventoDeDeletreo.PalabraTerminada>()

    @Test fun `un sostén largo confirma una sola vez`() {
        assertEquals(listOf("A"), letras(correr(Sost("A", 3000))))
    }

    @Test fun `un toque corto no es una letra`() {
        assertEquals(emptyList<String>(), letras(correr(Sost("A", 150), Hueco(400))))
    }

    @Test fun `la misma letra exige soltar`() {
        assertEquals(listOf("L"), letras(correr(Sost("L", 1500))))
        assertEquals(listOf("L", "L"), letras(correr(Sost("L", 600), Hueco(300), Sost("L", 600))))
    }

    @Test fun `un hueco corto no repite`() {
        assertEquals(listOf("L"), letras(correr(Sost("L", 600), Hueco(100, sinMano = false), Sost("L", 600))))
    }

    @Test fun `letras distintas no piden soltar`() {
        assertEquals(listOf("C", "A", "R"), letras(correr(Sost("C", 600), Sost("A", 600), Sost("R", 600))))
    }

    @Test fun `un parpadeo de un frame no duplica la letra`() {
        assertEquals(listOf("L"), letras(correr(Sost("L", 600), Sost("K", 0), Sost("L", 600))))
    }

    @Test fun `calle lleva dos L`() {
        val e = correr(Sost("C", 600), Sost("A", 600), Sost("L", 600), Hueco(300), Sost("L", 600), Sost("E", 600), Hueco(2000))
        val p = palabras(e)
        assertEquals(1, p.size)
        assertEquals(listOf("C", "A", "L", "L", "E"), p[0].letras)
        assertEquals("CALLE", p[0].resultado.mejor)
    }

    @Test fun `perro lleva dos R`() {
        val e = correr(Sost("P", 500), Sost("E", 500), Sost("R", 500), Hueco(300), Sost("R", 500), Sost("O", 500), Hueco(2000))
        assertEquals("PERRO", palabras(e)[0].resultado.mejor)
    }

    @Test fun `una letra dudosa se corrige`() {
        val e = correr(
            Sost("C", 600), Sost("A", 600), Sost("L", 600), Hueco(300), Sost("L", 600),
            Sost("O", 600, 0.4f), Hueco(2000),
        )
        assertEquals("CALLE", palabras(e)[0].resultado.mejor)
    }

    @Test fun `letras seguras y sin sentido no se esconden`() {
        val e = correr(Sost("X", 500), Hueco(300), Sost("X", 500), Hueco(300), Sost("X", 500), Sost("U", 500), Hueco(2000))
        val p = palabras(e)[0]
        assertNull(p.resultado.mejor)
        assertTrue(p.letras.isNotEmpty())
    }

    @Test fun `las palabras de dos letras deben coincidir exacto`() {
        val r = DecodificadorDePalabras(lista, config).decodificar(listOf(LetraObservada("P", 0.5f), LetraObservada("A", 0.5f)))
        assertNull(r.mejor)
    }

    @Test fun `la palabra solo termina tras la pausa`() {
        assertFalse(palabras(correr(Sost("A", 600), Hueco(900))).isNotEmpty())
        assertEquals(1, palabras(correr(Sost("A", 600), Hueco(1600))).size)
    }

    @Test fun `los rechazos entre letras no terminan la palabra`() {
        val e = correr(
            Sost("C", 600), Hueco(500, sinMano = false), Sost("A", 600), Hueco(500, sinMano = false),
            Sost("L", 600), Hueco(2000),
        )
        assertEquals(listOf("C", "A", "L"), palabras(e)[0].letras)
    }

    @Test fun `terminar fuerza el cierre`() {
        val m = motor()
        for (i in 0..18) m.registrar(FrameDeDeletreo.Letra(i * PASO, "M", 0.9f))
        val e = m.terminar(1000)
        assertEquals(listOf("M"), (e[0] as EventoDeDeletreo.PalabraTerminada).letras)
    }

    @Test fun `dos palabras seguidas`() {
        val e = correr(
            Sost("M", 600), Sost("A", 600), Sost("R", 600), Hueco(2000),
            Sost("L", 600), Sost("U", 600), Sost("N", 600), Sost("A", 600), Hueco(2000),
        )
        assertEquals(listOf("MAR", "LUNA"), palabras(e).map { it.resultado.mejor })
    }

    @Test fun `unidades y lista`() {
        assertEquals(listOf("C", "A", "L", "L", "E"), Palabras.aUnidades("calle"))
        assertEquals(listOf("A", "NN", "O"), Palabras.aUnidades("año"))
        assertEquals(listOf("C", "A", "F", "E"), Palabras.aUnidades("café"))
        assertTrue(lista.size > 200)
        assertTrue(lista.none { p -> p.unidades.any { it in config.letrasExcluidas } })
        assertTrue(lista.any { it.palabra == "CALLE" })
        assertTrue(lista.none { it.palabra == "CASA" })
    }

    private companion object {
        const val PASO = 33L
    }
}
