package com.ufps.signaco.core.reconocimiento.domain

/**
 * Contrato del reconocedor de senas. Es la frontera que aisla al resto de la app del modelo.
 *
 * Recibe los 63 valores (21 puntos x 3 coordenadas) que produce MediaPipe HandLandmarker
 * despues de normalizar, nunca imagenes. Un arreglo vacio significa que no se detecto mano.
 *
 * RNF-21: cambiar de implementacion es una sola linea en
 * [com.ufps.signaco.core.di.ReconocimientoModule]. Ni presentation ni domain se enteran.
 */
interface SignClassifier {
    suspend fun clasificar(landmarks: FloatArray): ResultadoClasificacion
}
