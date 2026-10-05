package com.ufps.signaco.feature.practica.domain.usecase

import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import javax.inject.Inject

/**
 * Umbral de confianza de una seña, tal como viene en config_rechazo.json. La pantalla lo dibuja
 * como la marca del medidor de coincidencia; no se escribe en el código.
 */
class ObtenerUmbralDeSenaUseCase @Inject constructor(
    private val proveedorDeConfiguracion: ProveedorDeConfiguracion,
) {
    suspend operator fun invoke(senaId: String): Float =
        proveedorDeConfiguracion.obtener().umbralDe(senaId)
}
