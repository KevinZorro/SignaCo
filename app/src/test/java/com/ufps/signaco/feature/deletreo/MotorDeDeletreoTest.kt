package com.ufps.signaco.feature.deletreo

import com.ufps.signaco.feature.deletreo.domain.ConfiguracionDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.DecodificadorDePalabras
import com.ufps.signaco.feature.deletreo.domain.EventoDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.FrameDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.Gestos
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
        assertEquals(listOf("A"), letras(correr(Sost("A", 3500))))
    }

    @Test fun `un toque corto no es una letra`() {
        assertEquals(emptyList<String>(), letras(correr(Sost("A", 150), Hueco(400))))
    }

    @Test fun `la misma letra exige soltar`() {
        assertEquals(listOf("L"), letras(correr(Sost("L", 1500))))
        assertEquals(listOf("L", "L"), letras(correr(Sost("L", 1400), Hueco(300), Sost("L", 1400))))
    }

    @Test fun `un hueco corto no repite`() {
        assertEquals(listOf("L"), letras(correr(Sost("L", 1400), Hueco(100, sinMano = false), Sost("L", 1400))))
    }

    @Test fun `letras distintas no piden soltar`() {
        assertEquals(listOf("C", "A", "R"), letras(correr(Sost("C", 1400), Sost("A", 1400), Sost("R", 1400))))
    }

    @Test fun `un parpadeo de un frame no duplica la letra`() {
        assertEquals(listOf("L"), letras(correr(Sost("L", 1400), Sost("K", 0), Sost("L", 1400))))
    }

    @Test fun `calle lleva dos L`() {
        val e = correr(Sost("C", 1400), Sost("A", 1400), Sost("L", 1400), Hueco(300), Sost("L", 1400), Sost("E", 1400), Hueco(2000))
        val p = palabras(e)
        assertEquals(1, p.size)
        assertEquals(listOf("C", "A", "L", "L", "E"), p[0].letras)
        assertEquals("CALLE", p[0].resultado.mejor)
    }

    @Test fun `perro lleva dos R`() {
        val e = correr(Sost("P", 1400), Sost("E", 1400), Sost("R", 1400), Hueco(300), Sost("R", 1400), Sost("O", 1400), Hueco(2000))
        assertEquals("PERRO", palabras(e)[0].resultado.mejor)
    }

    @Test fun `una letra dudosa se corrige`() {
        val e = correr(
            Sost("C", 1400), Sost("A", 1400), Sost("L", 1400), Hueco(300), Sost("L", 1400),
            Sost("O", 1400, 0.4f), Hueco(2000),
        )
        assertEquals("CALLE", palabras(e)[0].resultado.mejor)
    }

    @Test fun `letras seguras y sin sentido no se esconden`() {
        val e = correr(Sost("X", 1400), Hueco(300), Sost("X", 1400), Hueco(300), Sost("X", 1400), Sost("U", 1400), Hueco(2000))
        val p = palabras(e)[0]
        assertNull(p.resultado.mejor)
        assertTrue(p.letras.isNotEmpty())
    }

    @Test fun `las palabras de dos letras deben coincidir exacto`() {
        val r = DecodificadorDePalabras(lista, config).decodificar(listOf(LetraObservada("P", 0.5f), LetraObservada("A", 0.5f)))
        assertNull(r.mejor)
    }

    @Test fun `la palabra solo termina tras la pausa`() {
        assertFalse(palabras(correr(Sost("A", 1400), Hueco(900))).isNotEmpty())
        assertEquals(1, palabras(correr(Sost("A", 1400), Hueco(1600))).size)
    }

    @Test fun `los rechazos entre letras no terminan la palabra`() {
        val e = correr(
            Sost("C", 1400), Hueco(500, sinMano = false), Sost("A", 1400), Hueco(500, sinMano = false),
            Sost("L", 1400), Hueco(2000),
        )
        assertEquals(listOf("C", "A", "L"), palabras(e)[0].letras)
    }

    @Test fun `terminar fuerza el cierre`() {
        val m = motor()
        for (i in 0..45) m.registrar(FrameDeDeletreo.Letra(i * PASO, "M", 0.9f))
        val e = m.terminar(2000)
        assertEquals(listOf("M"), (e[0] as EventoDeDeletreo.PalabraTerminada).letras)
    }

    @Test fun `dos palabras seguidas`() {
        val e = correr(
            Sost("M", 1400), Sost("A", 1400), Sost("R", 1400), Hueco(2000),
            Sost("L", 1400), Sost("U", 1400), Sost("N", 1400), Sost("A", 1400), Hueco(2000),
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


    @Test fun `un sostén de 800 ms no basta y uno de 1400 sí`() {
        assertEquals(emptyList<String>(), letras(correr(Sost("A", 800), Hueco(300))))
        assertEquals(listOf("A"), letras(correr(Sost("A", 1400))))
    }

    @Test fun `las letras de los primeros 300 ms de una mano se ignoran`() {
        assertEquals(listOf("A"), letras(correr(Sost("O", 600), Sost("A", 1400))))
    }

    @Test fun `las letras de los bordes por subir y bajar la mano se descartan al decodificar`() {
        val obs = listOf("O" to 0.7f, "M" to 0.9f, "A" to 0.9f, "M" to 0.9f, "A" to 0.9f, "O" to 0.7f)
            .map { LetraObservada(it.first, it.second) }
        assertEquals("MAMA", DecodificadorDePalabras(lista, config).decodificar(obs).mejor)
    }

    @Test fun `borrar el borde no rescata un medio equivocado`() {
        val obs = List(4) { LetraObservada("X", 0.9f) }
        assertNull(DecodificadorDePalabras(lista, config).decodificar(obs).mejor)
    }

    @Test fun `el gesto de borrar quita la ultima letra`() {
        val e = correr(Sost("M", 1400), Sost("A", 1400), Sost("X", 1400), Sost(Gestos.BORRAR, 1400), Hueco(2000))
        assertEquals(listOf("X"), e.filterIsInstance<EventoDeDeletreo.LetraBorrada>().map { it.letra })
        assertEquals(listOf("M", "A"), palabras(e)[0].letras)
    }

    @Test fun `borrar sin letras pide quitar una palabra`() {
        val e = correr(Sost(Gestos.BORRAR, 1400))
        assertTrue(e.any { it is EventoDeDeletreo.BorrarPalabraSolicitado })
    }

    @Test fun `borrar exige soltar para repetirse`() {
        val e = correr(Sost("M", 1400), Sost("A", 1400), Sost("R", 1400), Sost(Gestos.BORRAR, 3500))
        assertEquals(1, e.filterIsInstance<EventoDeDeletreo.LetraBorrada>().size)
    }

    @Test fun `el gesto de espacio cierra la palabra de una vez`() {
        val e = correr(Sost("M", 1400), Sost("A", 1400), Sost("R", 1400), Sost(Gestos.ESPACIO, 1400))
        assertEquals(1, palabras(e).size)
        assertEquals("MAR", palabras(e)[0].resultado.mejor)
    }

    @Test fun `los gestos nunca entran como letras`() {
        val e = correr(Sost("M", 1400), Sost(Gestos.BORRAR, 1400), Sost("A", 1400), Sost(Gestos.ESPACIO, 1400))
        assertTrue(letras(e).none { it.startsWith("@") })
    }

    @Test fun `el progreso sube mientras se sostiene y se apaga al confirmar`() {
        val m = motor()
        for (i in 0..30) m.registrar(FrameDeDeletreo.Letra(i * PASO, "A", 0.9f))
        val p = m.progreso
        assertEquals("A", p.etiqueta)
        assertTrue(p.fraccion > 0.4f && p.fraccion < 0.95f)
        for (i in 31..80) m.registrar(FrameDeDeletreo.Letra(i * PASO, "A", 0.9f))
        assertNull(m.progreso.etiqueta)
    }

    private companion object {
        const val PASO = 33L
    }
}
