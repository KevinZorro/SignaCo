package com.ufps.signaco.feature.quiz.domain.usecase

import com.ufps.signaco.core.senas.domain.SenaRepository
import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import com.ufps.signaco.feature.quiz.domain.model.PreguntaQuiz
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

/**
 * Arma el quiz de reconocimiento de una lección: por cada letra de la lección que tenga imagen
 * de referencia, una pregunta con esa imagen y cuatro letras para elegir.
 *
 * - Solo se pregunta por letras practicables que tengan imagen: sin imagen no hay qué mostrar.
 * - Las opciones incorrectas salen primero de las demás letras de la lección, que son las que
 *   se confunden entre sí, y se completan con el resto del vocabulario activo.
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
        val deLaLeccion = leccion.senas.filter { it.disponible }.map { it.letra }
        val candidatas = leccion.senas.filter { it.disponible && it.referencia != null }
        if (candidatas.isEmpty()) return emptyList()

        val vocabulario = senaRepository.observarSenas().first()
            .filter { it.disponible }
            .map { it.letra }

        return candidatas
            .shuffled(random)
            .take(numeroDePreguntas)
            .map { sena ->
                val cercanas = deLaLeccion.filter { it != sena.letra }.shuffled(random)
                val lejanas = vocabulario.filter { it != sena.letra && it !in deLaLeccion }.shuffled(random)
                val distractores = (cercanas + lejanas).take(OPCIONES_POR_PREGUNTA - 1)
                PreguntaQuiz(
                    senaId = sena.id,
                    referencia = requireNotNull(sena.referencia),
                    letraCorrecta = sena.letra,
                    opciones = (distractores + sena.letra).shuffled(random),
                )
            }
    }

    companion object {
        /** Una por letra de la lección; las lecciones tienen entre 3 y 4. */
        const val PREGUNTAS_POR_QUIZ = 4
        const val OPCIONES_POR_PREGUNTA = 4
    }
}
