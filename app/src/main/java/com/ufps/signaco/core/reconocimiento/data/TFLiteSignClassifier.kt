package com.ufps.signaco.core.reconocimiento.data

import com.ufps.signaco.core.comun.DispatcherDefault
import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import com.ufps.signaco.core.reconocimiento.domain.SignClassifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ESQUELETO. Aqui se porta el interprete TFLite del spike. Hoy no se usa: el modulo de Hilt
 * enlaza [FakeSignClassifier]. Para activarlo se cambia esa unica linea (RNF-21).
 *
 * Antes de portar hay que agregar al build las dependencias de TensorFlow Lite y de
 * MediaPipe Tasks, y copiar a `app/src/main/assets`:
 *  - signaco_abecedario_rechazo.tflite  (59 KB, 28 clases: 27 letras + no_es_sena)
 *  - config_rechazo.json                (etiquetas, umbrales y parametro k de estabilidad)
 *  - hand_landmarker.task               (7.46 MB)
 *
 * QUE ESPERA ESTA CLASE COMO ENTRADA
 * Los 63 floats YA NORMALIZADOS. La normalizacion pertenece a la capa de captura, que es la
 * unica que conoce el `handedness` que reporta MediaPipe. El orden es exactamente este y no
 * se "mejora": si cambia, el modelo devuelve basura sin lanzar ningun error.
 *
 *  1. Restar a todos los puntos la posicion del landmark 0 (la muneca).
 *  2. Calcular la escala como la norma euclidiana maxima entre los puntos ya trasladados.
 *  3. Dividir todos los puntos por esa escala.
 *  4. Si handedness == "Left", multiplicar la coordenada X por -1.
 *  5. Aplanar como x0,y0,z0,...,x20,y20,z20.
 *
 * UMBRALES
 * El umbral de confianza y el parametro k viven en config_rechazo.json, no en el modelo ni
 * en el codigo. Se leen de assets al inicializar. No hardcodearlos.
 */
@Singleton
class TFLiteSignClassifier @Inject constructor(
    @DispatcherDefault private val dispatcher: CoroutineDispatcher,
) : SignClassifier {

    override suspend fun clasificar(landmarks: FloatArray): ResultadoClasificacion =
        withContext(dispatcher) {
            // TODO(equipo): portar el interprete del spike.
            //  1. Cargar el modelo desde assets una sola vez (perezoso, protegido por Mutex).
            //  2. Leer config_rechazo.json: etiquetas, umbral de confianza y k.
            //  3. Correr la inferencia sobre los 63 floats de entrada.
            //  4. Si la clase ganadora es Alfabeto.NO_ES_SENA o no llega al umbral,
            //     devolver SenaNoReconocida; si no, LetraReconocida con su confianza.
            //  5. ManoNoDetectada lo decide la capa de captura (arreglo vacio), no el modelo.
            //  6. Cerrar el interprete cuando se libere el clasificador.
            TODO("Pendiente: portar el interprete TFLite del spike")
        }
}
