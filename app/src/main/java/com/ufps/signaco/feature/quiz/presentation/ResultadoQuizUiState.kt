package com.ufps.signaco.feature.quiz.presentation

import com.ufps.signaco.feature.quiz.domain.usecase.SenaCalificada

data class ResultadoQuizUiState(
    val cargando: Boolean = true,
    val aciertos: Int = 0,
    val total: Int = 0,
    val aprobado: Boolean = false,
    /** Parte 1, con imagen. Se muestra pero no aprueba. Total 0 si no la hubo. */
    val aciertosImagen: Int = 0,
    val totalImagen: Int = 0,
    val tituloDeLeccion: String = "",
    /** Vacío si el desglose no se pudo cargar: el puntaje se muestra igual. */
    val senas: List<SenaCalificada> = emptyList(),
)
