package com.ufps.signaco.feature.quiz.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ufps.signaco.core.navegacion.Rutas
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz
import com.ufps.signaco.feature.quiz.domain.usecase.ObtenerDesgloseDeQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * El puntaje llega por la ruta, ya guardado por el quiz. Aquí solo se arma el desglose por
 * seña para mostrarlo; si falla, el puntaje se sigue mostrando sin la lista.
 */
@HiltViewModel
class ResultadoQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    obtenerDesglose: ObtenerDesgloseDeQuizUseCase,
) : ViewModel() {

    private val ruta = savedStateHandle.toRoute<Rutas.ResultadoQuiz>()
    val leccionId: String = ruta.leccionId

    private val base = ResultadoQuizUiState(
        cargando = false,
        aciertos = ruta.aciertos,
        total = ruta.total,
        aprobado = ResultadoQuiz(ruta.aciertos, ruta.total).aprobado,
    )

    val uiState: StateFlow<ResultadoQuizUiState> =
        obtenerDesglose(
            ruta.leccionId,
            ruta.senasDelQuiz,
            ruta.aciertosPorPregunta,
        )
            .map { desglose ->
                base.copy(
                    tituloDeLeccion = desglose?.tituloDeLeccion.orEmpty(),
                    senas = desglose?.senas.orEmpty(),
                )
            }
            .onStart { emit(base.copy(cargando = true)) }
            .catch { error ->
                Log.e(ETIQUETA, "No se pudo armar el desglose del quiz", error)
                emit(base)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIEMPO_DE_ESPERA_MS), base.copy(cargando = true))

    private companion object {
        const val ETIQUETA = "ResultadoQuiz"
        const val TIEMPO_DE_ESPERA_MS = 5_000L
    }
}
