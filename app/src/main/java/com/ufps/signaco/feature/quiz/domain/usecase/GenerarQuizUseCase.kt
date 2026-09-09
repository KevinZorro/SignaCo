package com.ufps.signaco.feature.quiz.domain.usecase

import com.ufps.signaco.core.senas.domain.SenaRepository
import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import com.ufps.signaco.feature.quiz.domain.model.PreguntaQuiz
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

/**
 * Arma el quiz de una lección: toma una foto del catálogo y genera las preguntas.
 *
 * No devuelve Flow a propósito: un quiz se genera una vez y no cambia mientras se responde.
 *
 * [random] se recibe como parámetro para que las pruebas puedan fijar la semilla y esperar
 * siempre el mismo cuestionario.
 */
class GenerarQuizUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val senaRepository: SenaRepository,
) {
    suspend operator fun invoke(
        leccionId: String,
        numeroDePreguntas: Int = PREGUNTAS_POR_QUIZ,
        random: Random = Random.Default,
    ): List<PreguntaQuiz> {
        val leccion = leccionRepository.observarLeccion(leccionId).first() ?: return emptyList()
        val candidatas = leccion.senas.filter { it.disponible }
        if (candidatas.isEmpty()) return emptyList()

        val vocabulario = senaRepository.observarSenas().first()
            .filter { it.disponible }
            .map { it.letra }

        return candidatas
            .shuffled(random)
            .take(numeroDePreguntas)
            .map { sena ->
                val distractores = vocabulario
                    .filter { it != sena.letra }
                    .shuffled(random)
                    .take(OPCIONES_POR_PREGUNTA - 1)
                PreguntaQuiz(
                    senaId = sena.id,
                    enunciado = sena.descripcion,
                    letraCorrecta = sena.letra,
                    opciones = (distractores + sena.letra).shuffled(random),
                )
            }
    }

    companion object {
        const val PREGUNTAS_POR_QUIZ = 5
        const val OPCIONES_POR_PREGUNTA = 4
    }
}
