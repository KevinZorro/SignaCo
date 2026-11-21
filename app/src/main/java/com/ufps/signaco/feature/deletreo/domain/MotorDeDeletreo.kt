package com.ufps.signaco.feature.deletreo.domain

/** Lo que dijo el clasificador en un frame, ya con el umbral aplicado. */
sealed interface FrameDeDeletreo {
    val tMs: Long

    data class Letra(override val tMs: Long, val letra: String, val confianza: Float) : FrameDeDeletreo
    data class Rechazada(override val tMs: Long) : FrameDeDeletreo
    data class SinMano(override val tMs: Long) : FrameDeDeletreo
}

sealed interface EventoDeDeletreo {
    data class LetraConfirmada(val tMs: Long, val letra: String, val confianza: Float) : EventoDeDeletreo
    data class PalabraTerminada(
        val tMs: Long,
        val letras: List<String>,
        val resultado: ResultadoDeDecodificacion,
    ) : EventoDeDeletreo
}

/**
 * Máquina de estados del deletreo con letras estáticas (réplica de lsc70/spelling.py).
 *
 * - Una letra se confirma al sostenerla [ConfiguracionDeDeletreo.sostenerMs].
 * - La MISMA letra otra vez exige soltar ([ConfiguracionDeDeletreo.soltarMs] sin letra): así
 *   L, soltar, L es LL. Una letra DISTINTA no pide soltar. Un parpadeo de otra letra no duplica.
 * - La palabra termina tras [ConfiguracionDeDeletreo.pausaDePalabraMs] sin letra y se decodifica.
 *
 * Sin reloj propio: el tiempo viene en los frames, así que las pruebas son deterministas.
 * Tiene estado: uno por sesión, no se comparte.
 */
class MotorDeDeletreo(
    private val config: ConfiguracionDeDeletreo,
    private val decodificador: DecodificadorDePalabras,
) {
    private val letras = ArrayList<LetraObservada>()
    private var candidata: String? = null
    private var inicioCandidata = 0L
    private var framesCandidata = 0
    private var sumaConfianza = 0f
    private var bloqueada: String? = null
    private var inicioSinLetra: Long? = null

    /** Letras ya confirmadas de la palabra en curso, para mostrarlas en vivo. */
    val letrasActuales: List<String> get() = letras.map { it.letra }

    fun registrar(frame: FrameDeDeletreo): List<EventoDeDeletreo> {
        val eventos = ArrayList<EventoDeDeletreo>(1)
        if (frame is FrameDeDeletreo.Letra) alLlegarLetra(frame, eventos) else alLlegarSinLetra(frame, eventos)
        return eventos
    }

    /** Fuerza el fin de la palabra (el usuario pulsó "listo"). */
    fun terminar(tMs: Long): List<EventoDeDeletreo> {
        val eventos = ArrayList<EventoDeDeletreo>(1)
        terminarPalabra(tMs, eventos)
        return eventos
    }

    fun reiniciar() {
        letras.clear()
        candidata = null
        inicioCandidata = 0L
        framesCandidata = 0
        sumaConfianza = 0f
        bloqueada = null
        inicioSinLetra = null
    }

    private fun alLlegarLetra(f: FrameDeDeletreo.Letra, eventos: MutableList<EventoDeDeletreo>) {
        val sinLetra = inicioSinLetra?.let { f.tMs - it } ?: 0L
        inicioSinLetra = null
        val solto = sinLetra >= config.soltarMs
        if (solto) bloqueada = null
        if (f.letra != candidata || solto) {
            candidata = f.letra
            inicioCandidata = f.tMs
            framesCandidata = 0
            sumaConfianza = 0f
        }
        framesCandidata++
        sumaConfianza += f.confianza
        val sostenida = f.tMs - inicioCandidata
        if (sostenida >= config.sostenerMs && framesCandidata >= config.minimoDeFrames && f.letra != bloqueada) {
            val confianza = sumaConfianza / framesCandidata
            letras.add(LetraObservada(f.letra, confianza))
            bloqueada = f.letra
            eventos.add(EventoDeDeletreo.LetraConfirmada(f.tMs, f.letra, confianza))
            if (letras.size >= config.maximoDeLetras) terminarPalabra(f.tMs, eventos)
        }
    }

    private fun alLlegarSinLetra(f: FrameDeDeletreo, eventos: MutableList<EventoDeDeletreo>) {
        val inicio = inicioSinLetra ?: f.tMs.also { inicioSinLetra = it }
        if (letras.isNotEmpty() && f.tMs - inicio >= config.pausaDePalabraMs) terminarPalabra(f.tMs, eventos)
    }

    private fun terminarPalabra(tMs: Long, eventos: MutableList<EventoDeDeletreo>) {
        if (letras.isNotEmpty()) {
            val observadas = letras.toList()
            eventos.add(
                EventoDeDeletreo.PalabraTerminada(
                    tMs = tMs,
                    letras = observadas.map { it.letra },
                    resultado = decodificador.decodificar(observadas),
                ),
            )
        }
        reiniciar()
        inicioSinLetra = tMs
    }
}
