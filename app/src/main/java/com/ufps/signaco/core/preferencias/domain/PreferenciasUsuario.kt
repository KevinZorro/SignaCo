package com.ufps.signaco.core.preferencias.domain

/**
 * Ajustes del usuario. Van en DataStore, no en Room: son preferencias, no datos.
 *
 * @param mostrarPistas si la pantalla de practica muestra la imagen de referencia de la seña.
 * @param retroalimentacionHaptica vibrar al acertar. RNF-09: toda señal sonora tiene que
 *        tener equivalente visual o háptico, porque parte de los usuarios son personas sordas.
 * @param manoDominante solo espeja las ilustraciones de la mano. No toca la normalización de
 *        landmarks, que usa el handedness que reporta MediaPipe.
 * @param velocidadDeClips y [subtitulosEnClips] se guardan para los clips de las señas, que la
 *        app todavía no tiene.
 * @param primerUsoCompletado false hasta que la persona pasa por «Primero conoce tu mano».
 */
data class PreferenciasUsuario(
    val mostrarPistas: Boolean = true,
    val retroalimentacionHaptica: Boolean = true,
    val manoDominante: ManoDominante = ManoDominante.DERECHA,
    val velocidadDeClips: VelocidadDeClips = VelocidadDeClips.MEDIA,
    val subtitulosEnClips: Boolean = false,
    val tamanoDeTexto: TamanoDeTexto = TamanoDeTexto.NORMAL,
    val altoContraste: Boolean = false,
    val reducirAnimaciones: Boolean = false,
    val primerUsoCompletado: Boolean = false,
    val recordatoriosActivos: Boolean = false,
)

enum class ManoDominante { DERECHA, IZQUIERDA }

enum class VelocidadDeClips { LENTA, MEDIA, NORMAL }

/** [escala] se multiplica sobre la escala de fuente del sistema, no la reemplaza. */
enum class TamanoDeTexto(val escala: Float) {
    NORMAL(1f),
    GRANDE(1.15f),
    MUY_GRANDE(1.3f),
}
