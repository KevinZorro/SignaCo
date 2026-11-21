package com.ufps.signaco.feature.deletreo.domain

data class DatosDeDeletreo(
    val config: ConfiguracionDeDeletreo,
    val lista: List<PalabraDeLista>,
)

/** Da la configuración y la lista cerrada de palabras sin que el dominio sepa de assets. */
interface ProveedorDeDeletreo {
    suspend fun obtener(): DatosDeDeletreo
}
