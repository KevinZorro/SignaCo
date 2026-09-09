package com.ufps.signaco.feature.practica.domain.model

/**
 * ANDAMIO TEMPORAL. Los desenlaces que la pantalla de práctica puede forzar mientras no
 * existe la cámara. Desaparece junto con [com.ufps.signaco.feature.practica.domain.usecase.SimularIntentoUseCase].
 */
enum class TipoDeSimulacion {
    SENA_CORRECTA,
    OTRA_LETRA,
    NO_RECONOCIDA,
    MANO_FUERA,
}
