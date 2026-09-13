package com.ufps.signaco.core.reconocimiento.domain

/**
 * El modelo o su configuración no se pudieron cargar.
 *
 * Se lanza en vez de devolver [ResultadoClasificacion.SenaNoReconocida] a propósito: un modelo
 * que no cargó no es "no te reconocí la seña", es un fallo que el usuario tiene que poder ver
 * y reintentar (RNF-13). Confundir ambos casos dejaría la app diciendo "intenta de nuevo"
 * para siempre.
 */
class ErrorDeModelo(mensaje: String, causa: Throwable? = null) : Exception(mensaje, causa)
