package com.ufps.signaco.feature.quiz.domain.usecase

import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import com.ufps.signaco.feature.quiz.domain.model.PreguntaDeCamara
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

/**
 * Arma el quiz con cámara de una lección (RF15): [PREGUNTAS_POR_QUIZ] letras de la lección para
 * hacer frente a la cámara.
 *
 * - Cada letra practicable de la lección sale al menos una vez; las preguntas que sobran se
 *   reparten al azar sin que una letra salga más de una vez por encima de las demás.
 * - Una letra puede repetirse, pero nunca en dos preguntas seguidas (salvo en una lección de una
 *   sola letra, donde no hay otra opción).
 *
 * [random] se recibe como parámetro para que las pruebas fijen la semilla.
 */
class GenerarQuizDeCamaraUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
) {
    suspend operator fun invoke(
        leccionId: String,
        numeroDePreguntas: Int = PREGUNTAS_POR_QUIZ,
        random: Random = Random.Default,
    ): List<PreguntaDeCamara> {
        val leccion = leccionRepository.observarLeccion(leccionId).first() ?: return emptyList()
        val letras = leccion.senas
            .filter { it.disponible }
            .map { PreguntaDeCamara(senaId = it.id, letra = it.letra) }
        if (letras.isEmpty() || numeroDePreguntas <= 0) return emptyList()

        // Cuántas veces sale cada letra: todas una vez y las sobrantes repartidas por vueltas,
        // así ninguna sale dos veces más que otra.
        val veces = mutableMapOf<PreguntaDeCamara, Int>()
        var pendientes = numeroDePreguntas
        while (pendientes > 0) {
            letras.shuffled(random).take(pendientes).forEach { veces[it] = (veces[it] ?: 0) + 1 }
            pendientes -= minOf(pendientes, letras.size)
        }
        return ordenarSinRepetirSeguidas(veces, random)
    }

    /**
     * En cada posición toma, entre las letras distintas de la anterior, una de las que más veces
     * le faltan. Elegir la que más falta es lo que evita quedarse al final con la misma letra
     * dos veces seguidas.
     */
    private fun ordenarSinRepetirSeguidas(
        veces: MutableMap<PreguntaDeCamara, Int>,
        random: Random,
    ): List<PreguntaDeCamara> {
        val orden = mutableListOf<PreguntaDeCamara>()
        while (veces.values.any { it > 0 }) {
            val disponibles = veces.filter { (letra, faltan) -> faltan > 0 && letra != orden.lastOrNull() }
                .ifEmpty { veces.filterValues { it > 0 } }
            val maximo = disponibles.values.max()
            val elegida = disponibles.filterValues { it == maximo }.keys.toList().random(random)
            orden += elegida
            veces[elegida] = veces.getValue(elegida) - 1
        }
        return orden
    }

    companion object {
        /** Con 5 preguntas, aprobar (70 %) son 4 aciertos; con 3 harían falta todas. */
        const val PREGUNTAS_POR_QUIZ = 5
    }
}
