package com.ufps.signaco.feature.quiz.domain.usecase

import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.feature.lecciones.domain.usecase.ObtenerDetalleDeLeccionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Una pregunta del quiz ya resuelta, con lo que la pantalla necesita para listarla. */
data class SenaCalificada(
    val sena: Sena,
    val acerto: Boolean,
    val dominada: Boolean,
)

data class DesgloseDeQuiz(
    val tituloDeLeccion: String,
    val senas: List<SenaCalificada>,
) {
    val porRepasar: List<Sena> get() = senas.filterNot { it.acerto }.map { it.sena }
}

/**
 * Arma el desglose del resultado: cada seña del quiz, en el orden en que se preguntó, con si
 * se acertó y si ya está dominada. El quiz no suma intentos, así que no cambia el estado de
 * dominada: solo se informa.
 *
 * Emite null si la lección no existe.
 */
class ObtenerDesgloseDeQuizUseCase @Inject constructor(
    private val obtenerDetalle: ObtenerDetalleDeLeccionUseCase,
) {
    operator fun invoke(
        leccionId: String,
        senasDelQuiz: List<String>,
        senasFalladas: List<String>,
    ): Flow<DesgloseDeQuiz?> = obtenerDetalle(leccionId).map { leccion ->
        if (leccion == null) return@map null
        val porId = leccion.senas.associateBy { it.sena.id }
        DesgloseDeQuiz(
            tituloDeLeccion = leccion.leccion.titulo,
            senas = senasDelQuiz.mapNotNull { id ->
                val conProgreso = porId[id] ?: return@mapNotNull null
                SenaCalificada(
                    sena = conProgreso.sena,
                    acerto = id !in senasFalladas,
                    dominada = conProgreso.dominada,
                )
            },
        )
    }
}
