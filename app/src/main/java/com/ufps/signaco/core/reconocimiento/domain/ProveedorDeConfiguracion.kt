package com.ufps.signaco.core.reconocimiento.domain

/**
 * Da acceso a config_rechazo.json sin que el dominio sepa que existe la carpeta assets.
 * La implementación vive en data, igual que los repositorios.
 */
interface ProveedorDeConfiguracion {
    suspend fun obtener(): ConfiguracionDeReconocimiento
}
