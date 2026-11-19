package com.ufps.signaco.feature.practica.domain.usecase

import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import com.ufps.signaco.feature.practica.domain.EstabilizadorDePredicciones
import javax.inject.Inject

/**
 * Crea un estabilizador con k, ventanaMs y fraccionMinima que vienen de la configuración
 * del reconocimiento.
 *
 * Existe para que el ViewModel no tenga que saber de dónde salen los parámetros: pide un
 * estabilizador y el dominio se lo entrega ya configurado. Se crea uno por sesión de práctica.
 */
class CrearEstabilizadorUseCase @Inject constructor(
    private val proveedorDeConfiguracion: ProveedorDeConfiguracion,
) {
    suspend operator fun invoke(): EstabilizadorDePredicciones {
        val config = proveedorDeConfiguracion.obtener()
        return EstabilizadorDePredicciones(
            k = config.k,
            ventanaMs = config.ventanaMs,
            fraccionMinima = config.fraccionMinima,
        )
    }
}
