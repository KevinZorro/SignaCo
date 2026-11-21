package com.ufps.signaco.feature.deletreo.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class LetraObservada(val letra: String, val confianza: Float)

data class Candidata(val palabra: String, val costo: Float)

data class ResultadoDeDecodificacion(
    /** Null: nada de la lista está lo bastante cerca; se muestran las letras tal cual. */
    val mejor: String?,
    val candidatas: List<Candidata>,
    /** La segunda candidata está a menos de [ConfiguracionDeDeletreo.margenDeAmbiguedad]. */
    val ambigua: Boolean,
)

/**
 * Distancia de edición ponderada contra la lista cerrada. Insertar o borrar cuesta 1; sustituir
 * cuesta la confianza de la letra observada (con un piso), de modo que una letra dudosa se
 * corrige barato y una segura casi nunca. Réplica de lsc70/spelling.py: si una cambia, la otra
 * también, y los casos de prueba son los mismos.
 */
class DecodificadorDePalabras(
    private val lista: List<PalabraDeLista>,
    private val config: ConfiguracionDeDeletreo,
) {

    fun decodificar(observadas: List<LetraObservada>): ResultadoDeDecodificacion {
        if (observadas.isEmpty()) return ResultadoDeDecodificacion(null, emptyList(), false)
        val limite = config.costoMaximo(observadas.size)
        val puntuadas = ArrayList<Triple<Float, Int, String>>()
        lista.forEachIndexed { prioridad, p ->
            if (abs(p.unidades.size - observadas.size) > 2) return@forEachIndexed
            puntuadas.add(Triple(distancia(observadas, p.unidades), prioridad, p.palabra))
        }
        puntuadas.sortWith(compareBy({ it.first }, { it.second }))
        val buenas = puntuadas
            .filter { it.first <= limite + EPSILON }
            .take(config.maximoDeCandidatas)
            .map { Candidata(it.third, it.first) }
        if (buenas.isEmpty()) return ResultadoDeDecodificacion(null, emptyList(), false)
        val ambigua = buenas.size > 1 && buenas[1].costo - buenas[0].costo < config.margenDeAmbiguedad
        return ResultadoDeDecodificacion(buenas[0].palabra, buenas, ambigua)
    }

    internal fun distancia(observadas: List<LetraObservada>, objetivo: List<String>): Float {
        val m = objetivo.size
        var previa = FloatArray(m + 1) { it.toFloat() }
        for (i in 1..observadas.size) {
            val actual = FloatArray(m + 1)
            actual[0] = i.toFloat()
            val o = observadas[i - 1]
            val sustituir = max(config.costoMinimoDeSustitucion, min(1f, o.confianza))
            for (j in 1..m) {
                val igual = o.letra == objetivo[j - 1]
                actual[j] = min(
                    min(previa[j] + 1f, actual[j - 1] + 1f),
                    previa[j - 1] + if (igual) 0f else sustituir,
                )
            }
            previa = actual
        }
        return previa[m]
    }

    private companion object {
        const val EPSILON = 1e-6f
    }
}
