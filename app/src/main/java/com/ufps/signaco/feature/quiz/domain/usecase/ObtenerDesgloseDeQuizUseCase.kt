package com.ufps.signaco.feature.quiz.domain.usecase

import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerDetalleDeLeccionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Una letra del quiz con cámara ya resuelta. Si la letra salió varias veces, se agrupa.
 *
 * @param aciertos en cuántas de sus [preguntas] la hizo bien.
 */
data class SenaCalificada(
    val sena: Sena,
    val preguntas: Int,
    val aciertos: Int,
    val dominada: Boolean,
) {
    /** La acertó todas las veces que salió. */
    val acerto: Boolean get() = aciertos == preguntas
}

data class DesgloseDeQuiz(
    val tituloDeLeccion: String,
    val senas: List<SenaCalificada>,
) {
    /** Las letras que falló al menos una vez. */
    val porRepasar: List<Sena> get() = senas.filterNot { it.acerto }.map { it.sena }
}

/**
 * Arma el desglose del resultado: cada letra del quiz con cámara, en el orden en que apareció por
 * primera vez, con cuántas veces la acertó y si ya está dominada. Como el quiz con cámara
 * registra intentos, «dominada» ya refleja lo que se hizo en el quiz.
 *
 * Emite null si la lección no existe.
 *
 * @param senasDelQuiz una por pregunta, en orden, con repeticiones.
 * @param aciertos uno por pregunta, en el mismo orden.
 */
class ObtenerDesgloseDeQuizUseCase @Inject constructor(
    private val obtenerDetalle: ObtenerDetalleDeLeccionUseCase,
) {
    operator fun invoke(
        leccionId: String,
        senasDelQuiz: List<String>,
        aciertos: List<Boolean>,
    ): Flow<DesgloseDeQuiz?> = obtenerDetalle(leccionId).map { leccion ->
        if (leccion == null) return@map null
        val porId = leccion.senas.associateBy { it.sena.id }
        val respuestas = senasDelQuiz.zip(aciertos)
        DesgloseDeQuiz(
            tituloDeLeccion = leccion.leccion.titulo,
            senas = senasDelQuiz.distinct().mapNotNull { id ->
                val conProgreso = porId[id] ?: return@mapNotNull null
                val deEstaSena = respuestas.filter { it.first == id }
                SenaCalificada(
                    sena = conProgreso.sena,
                    preguntas = deEstaSena.size,
                    aciertos = deEstaSena.count { it.second },
                    dominada = conProgreso.dominada,
                )
            },
        )
    }
}
