package com.ufps.signaco.core.reconocimiento.data

import android.util.Log
import com.ufps.signaco.BuildConfig
import com.ufps.signaco.core.reconocimiento.domain.ConfiguracionDeReconocimiento
import com.ufps.signaco.core.reconocimiento.domain.NormalizadorDeLandmarks
import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import com.ufps.signaco.core.reconocimiento.domain.VocabularioActivo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DIAGNÓSTICO TEMPORAL. Se borra en cuanto conteste la pregunta que lo motiva.
 *
 * La pregunta: el modelo se entrenó sobre recortes de 120x120, que son CUADRADOS. La cámara
 * entrega 3:4. MediaPipe divide `x` entre el ancho y `y` entre el alto, así que con una imagen
 * 3:4 el eje `y` llega comprimido un 25 % respecto a `x`, y ni trasladar a la muñeca ni dividir
 * por la norma máxima lo arreglan: eso quita el tamaño, no la deformación.
 *
 * Si la hipótesis es cierta, el modelo está viendo una mano achatada en TODOS los frames y por
 * eso no reconoce nada.
 *
 * En vez de cambiar el pipeline a ciegas, esto pasa el MISMO frame por el modelo dos veces —tal
 * como está hoy y con la relación de aspecto corregida— y registra cuál gana. Una captura de
 * logcat y se decide con el dato delante.
 *
 * No altera lo que ve el usuario: la app sigue clasificando por [TFLiteSignClassifier.clasificar]
 * con el vector de siempre. Esto solo mira y anota, y solo en debug.
 */
@Singleton
class DiagnosticoDeAspecto @Inject constructor(
    private val clasificador: TFLiteSignClassifier,
    private val proveedorDeConfiguracion: ProveedorDeConfiguracion,
) {

    private var frames = 0
    private val marcador = Marcador()

    /**
     * Bandera PROPIA, a posta separada de la de [RegistroDeDepuracion]. El volcado por frame de
     * ese registro son 63 floats y 28 probabilidades treinta veces por segundo, y logcat acaba
     * descartando líneas justo las que interesan. Si estorba, se pone `activo = false` en
     * [RegistroDeDepuracion] y esta comparación sigue imprimiendo.
     */
    var activo: Boolean = BuildConfig.DEBUG

    suspend fun comparar(puntosCrudos: FloatArray, esManoIzquierda: Boolean, ancho: Int, alto: Int) {
        if (!activo) return
        if (puntosCrudos.size != NormalizadorDeLandmarks.VALORES) return
        if (ancho <= 0 || alto <= 0) return

        val configuracion = proveedorDeConfiguracion.obtener()
        val activos = VocabularioActivo.indicesActivosEn(configuracion.etiquetas)

        val comoEstaHoy = NormalizadorDeLandmarks
            .normalizar(puntosCrudos, esManoIzquierda).valores
        val corregido = NormalizadorDeLandmarks
            .normalizar(
                NormalizadorDeLandmarks.corregirRelacionDeAspecto(puntosCrudos, ancho, alto),
                esManoIzquierda,
            ).valores

        val sinCorregir = leer(clasificador.probabilidadesDe(comoEstaHoy), configuracion, activos)
        val conCorreccion = leer(clasificador.probabilidadesDe(corregido), configuracion, activos)
        if (sinCorregir == null || conCorreccion == null) return

        frames++
        marcador.anotar(sinCorregir, conCorreccion)

        Log.i(
            ETIQUETA,
            "[ASPECTO] #%d  imagen %dx%d  (factor aplicado a y: x%.3f)"
                .format(frames, ancho, alto, alto.toFloat() / ancho.toFloat()),
        )
        Log.i(ETIQUETA, "[ASPECTO]   COMO ESTA HOY : ${sinCorregir.describir()}")
        Log.i(ETIQUETA, "[ASPECTO]   CORRIGIENDO Y : ${conCorreccion.describir()}")

        if (frames % FRAMES_POR_RESUMEN == 0) {
            Log.w(ETIQUETA, "[ASPECTO] ===== RESUMEN TRAS $frames FRAMES =====")
            Log.w(ETIQUETA, "[ASPECTO]   como esta hoy : ${marcador.resumenSinCorregir()}")
            Log.w(ETIQUETA, "[ASPECTO]   corrigiendo y : ${marcador.resumenCorregido()}")
            Log.w(ETIQUETA, "[ASPECTO]   ${marcador.veredicto()}")
        }
    }

    fun reiniciar() {
        frames = 0
        marcador.reiniciar()
    }

    private fun leer(
        probabilidades: FloatArray,
        configuracion: ConfiguracionDeReconocimiento,
        activos: IntArray,
    ): Lectura? {
        if (probabilidades.isEmpty()) return null

        var ganador = 0
        for (indice in probabilidades.indices) {
            if (probabilidades[indice] > probabilidades[ganador]) ganador = indice
        }
        var mejorActiva = activos.firstOrNull() ?: return null
        for (indice in activos) {
            if (probabilidades[indice] > probabilidades[mejorActiva]) mejorActiva = indice
        }

        val letraActiva = configuracion.letraEn(mejorActiva)
        return Lectura(
            nombreGanador = if (ganador == configuracion.indiceDeRechazo) {
                RECHAZO
            } else {
                configuracion.letraEn(ganador) ?: "?$ganador"
            },
            probabilidadGanador = probabilidades[ganador],
            letraActiva = letraActiva,
            probabilidadActiva = probabilidades[mejorActiva],
            umbralActiva = letraActiva?.let { configuracion.umbralDe(it) }
                ?: configuracion.umbralGlobal,
            ganoElRechazo = ganador == configuracion.indiceDeRechazo,
            suma = probabilidades.sum(),
        )
    }

    private data class Lectura(
        val nombreGanador: String,
        val probabilidadGanador: Float,
        val letraActiva: String?,
        val probabilidadActiva: Float,
        val umbralActiva: Float,
        val ganoElRechazo: Boolean,
        val suma: Float,
    ) {
        /** La letra se aceptaría con las reglas que ya usa el clasificador. */
        val seAceptaria: Boolean
            get() = !ganoElRechazo && letraActiva != null && probabilidadActiva >= umbralActiva

        fun describir(): String =
            ("top1=%-11s %.4f | mejor activa=%-3s %.4f (umbral %.4f) | %s | suma=%.3f")
                .format(
                    nombreGanador, probabilidadGanador,
                    letraActiva ?: "-", probabilidadActiva, umbralActiva,
                    if (seAceptaria) "SE ACEPTA" else "se rechaza",
                    suma,
                )
    }

    /**
     * Cuenta de cada variante, para no tener que leer sesenta frames a ojo. Lo que decide no es
     * cuál da la letra correcta —eso solo lo sabes tú, que estás haciendo la seña— sino cuál
     * deja de caer en `no_es_seña` y con qué confianza.
     */
    private class Marcador {
        private var aceptadosSin = 0
        private var rechazoSin = 0
        private var sumaConfianzaSin = 0f
        private var aceptadosCon = 0
        private var rechazoCon = 0
        private var sumaConfianzaCon = 0f
        private var total = 0

        fun anotar(sinCorregir: Lectura, conCorreccion: Lectura) {
            total++
            if (sinCorregir.seAceptaria) aceptadosSin++
            if (sinCorregir.ganoElRechazo) rechazoSin++
            sumaConfianzaSin += sinCorregir.probabilidadActiva
            if (conCorreccion.seAceptaria) aceptadosCon++
            if (conCorreccion.ganoElRechazo) rechazoCon++
            sumaConfianzaCon += conCorreccion.probabilidadActiva
        }

        fun resumenSinCorregir(): String = linea(aceptadosSin, rechazoSin, sumaConfianzaSin)

        fun resumenCorregido(): String = linea(aceptadosCon, rechazoCon, sumaConfianzaCon)

        fun veredicto(): String = when {
            total == 0 -> "sin datos todavia"
            aceptadosCon > aceptadosSin * 2 && aceptadosCon > total / 4 ->
                "VEREDICTO: corregir la relacion de aspecto gana con claridad."
            aceptadosSin > aceptadosCon * 2 && aceptadosSin > total / 4 ->
                "VEREDICTO: la relacion de aspecto NO era el problema; buscar por otro lado."
            aceptadosSin == 0 && aceptadosCon == 0 ->
                "VEREDICTO: ninguna de las dos reconoce nada. El problema no es el aspecto; " +
                    "mirar la deteccion de MediaPipe o la carga del modelo."
            else -> "VEREDICTO: empate o diferencia pequena. Hacen falta mas frames, o es otra cosa."
        }

        fun reiniciar() {
            aceptadosSin = 0; rechazoSin = 0; sumaConfianzaSin = 0f
            aceptadosCon = 0; rechazoCon = 0; sumaConfianzaCon = 0f
            total = 0
        }

        private fun linea(aceptados: Int, rechazo: Int, sumaConfianza: Float): String =
            "aceptaria letra en %d/%d frames | gano no_es_sena en %d | confianza media de la mejor activa %.4f"
                .format(aceptados, total, rechazo, if (total == 0) 0f else sumaConfianza / total)
    }

    private companion object {
        const val ETIQUETA = "SignaCO_Debug"
        const val RECHAZO = "no_es_sena"
        const val FRAMES_POR_RESUMEN = 30
    }
}
