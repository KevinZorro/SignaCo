package com.ufps.signaco.feature.practica.domain.usecase

import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import com.ufps.signaco.feature.practica.domain.EstabilizadorDePredicciones
import javax.inject.Inject

/**
 * Crea un estabilizador con la k que trae config_rechazo.json.
 *
 * Existe para que el ViewModel no tenga que saber de dónde sale la k: pide un estabilizador y
 * el dominio se lo entrega ya configurado. Se crea uno por sesión de práctica.
 */
class CrearEstabilizadorUseCase @Inject constructor(
    private val proveedorDeConfiguracion: ProveedorDeConfiguracion,
) {
    suspend operator fun invoke(): EstabilizadorDePredicciones =
        EstabilizadorDePredicciones(proveedorDeConfiguracion.obtener().k)
}
