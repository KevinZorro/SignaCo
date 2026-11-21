package com.ufps.signaco.feature.deletreo.domain.usecase

import com.ufps.signaco.feature.deletreo.domain.DecodificadorDePalabras
import com.ufps.signaco.feature.deletreo.domain.MotorDeDeletreo
import com.ufps.signaco.feature.deletreo.domain.ProveedorDeDeletreo
import javax.inject.Inject

/** Crea un motor ya configurado. Tiene estado: se crea UNO por sesión de deletreo. */
class CrearMotorDeDeletreoUseCase @Inject constructor(
    private val proveedor: ProveedorDeDeletreo,
) {
    suspend operator fun invoke(): MotorDeDeletreo {
        val datos = proveedor.obtener()
        return MotorDeDeletreo(datos.config, DecodificadorDePalabras(datos.lista, datos.config))
    }
}
