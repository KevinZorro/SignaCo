package com.ufps.signaco.core.reconocimiento.domain

/**
 * Los tres desenlaces posibles de clasificar un frame. Son tres, no dos: "no hay mano"
 * y "hay mano pero la sena no corresponde a ninguna letra" se le explican distinto al usuario.
 *
 * El umbral de confianza no se aplica aqui ni en los casos de uso: vive en
 * config_rechazo.json y lo aplica la implementacion del clasificador. Cuando llega un
 * [LetraReconocida] ya paso el umbral.
 */
sealed interface ResultadoClasificacion {

    /** La sena corresponde a una letra del vocabulario, con la confianza reportada por el modelo. */
    data class LetraReconocida(val letra: String, val confianza: Float) : ResultadoClasificacion

    /**
     * Hay mano en cuadro, pero la pose no corresponde a ninguna letra (clase `no_es_sena`) o la
     * mejor letra no pasó su umbral.
     *
     * [candidata] y [confianza] son la mejor letra activa y su probabilidad cruda, aunque no
     * alcance el umbral. Solo sirven para mostrar la coincidencia en vivo; no deciden nada.
     */
    data class SenaNoReconocida(
        val candidata: String? = null,
        val confianza: Float = 0f,
    ) : ResultadoClasificacion

    /** No se detecto mano en el frame. */
    data object ManoNoDetectada : ResultadoClasificacion
}
