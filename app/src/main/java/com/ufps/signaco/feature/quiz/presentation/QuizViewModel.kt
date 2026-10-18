package com.ufps.signaco.feature.quiz.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ufps.signaco.core.navegacion.Rutas
import com.ufps.signaco.feature.progreso.domain.usecase.ActualizarRachaUseCase
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz
import com.ufps.signaco.feature.quiz.domain.usecase.CalificarQuizUseCase
import com.ufps.signaco.feature.quiz.domain.usecase.GenerarQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Parte 1 del quiz: reconocimiento con imagen. Se genera una vez al abrir la pantalla y no se
 * regenera mientras se responde.
 *
 * Esta parte no aprueba la lección (eso lo hace la parte con cámara) ni registra intentos de
 * seña: reconocer una imagen no es ejecutar la seña. Solo cuenta como actividad del día.
 */
@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val generarQuiz: GenerarQuizUseCase,
    private val calificarQuiz: CalificarQuizUseCase,
    private val actualizarRacha: ActualizarRachaUseCase,
) : ViewModel() {

    val leccionId: String = savedStateHandle.toRoute<Rutas.Quiz>().leccionId

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    /** Deja de ser null cuando el quiz terminó; la pantalla lo usa para navegar al resultado. */
    private val _resultado = MutableStateFlow<ResultadoQuiz?>(null)
    val resultado: StateFlow<ResultadoQuiz?> = _resultado.asStateFlow()

    init {
        cargarQuiz()
    }

    fun cargarQuiz() {
        _uiState.update { QuizUiState(cargando = true) }
        viewModelScope.launch {
            runCatching { generarQuiz(leccionId) }
                .onSuccess { preguntas ->
                    _uiState.update { QuizUiState(cargando = false, preguntas = preguntas) }
                    // Sin letras con imagen no hay parte 1: se pasa directo a la de cámara.
                    if (preguntas.isEmpty()) _resultado.value = ResultadoQuiz(aciertos = 0, total = 0)
                }
                .onFailure { error ->
                    Log.e(ETIQUETA, "No se pudo generar el quiz de $leccionId", error)
                    _uiState.update { QuizUiState(cargando = false, hayError = true) }
                }
        }
    }

    fun responder(letra: String) {
        val pregunta = _uiState.value.preguntaActual ?: return
        _uiState.update { estado ->
            estado.copy(respuestas = estado.respuestas + (pregunta.senaId to letra))
        }
    }

    fun siguientePregunta() {
        _uiState.update { estado ->
            if (estado.esLaUltima) estado else estado.copy(indiceActual = estado.indiceActual + 1)
        }
    }

    fun terminarQuiz() {
        val estado = _uiState.value
        if (estado.preguntas.isEmpty()) return
        viewModelScope.launch {
            val resultado = calificarQuiz(estado.preguntas, estado.respuestas)
            runCatching { actualizarRacha() }
                .onFailure { Log.e(ETIQUETA, "No se pudo registrar la actividad del día", it) }
            _resultado.value = resultado
        }
    }

    private companion object {
        const val ETIQUETA = "Quiz"
    }
}
