package com.ufps.signaco.feature.practica.domain.usecase

import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import com.ufps.signaco.feature.practica.domain.EstabilizadorDePredicciones
import javax.inject.Inject

/**
 * Crea un estabilizador con la k que trae config_rechazo.json y la ventana de 1 segundo (1.000 ms)
 * para promediar la seña que muestra y asegurar que no tome solo el primer frame que aparezca.
 *
 * Existe para que el ViewModel no tenga que saber de dónde sale la k: pide un estabilizador y
 * el dominio se lo entrega ya configurado. Se crea uno por sesión de práctica.
 */
class CrearEstabilizadorUseCase @Inject constructor(
    private val proveedorDeConfiguracion: ProveedorDeConfiguracion,
) {
    suspend operator fun invoke(
        duracionMinimaMs: Long = VENTANA_ESTABILIDAD_MS,
    ): EstabilizadorDePredicciones =
        EstabilizadorDePredicciones(
            k = proveedorDeConfiguracion.obtener().k,
            duracionMinimaMs = duracionMinimaMs,
        )

    companion object {
        /** Ventana de 1 segundo para confirmar la seña y promediar su visualización. */
        const val VENTANA_ESTABILIDAD_MS = 1_000L
    }
}
