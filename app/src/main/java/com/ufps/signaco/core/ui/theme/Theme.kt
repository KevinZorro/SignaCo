package com.ufps.signaco.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/** Colores de la paleta que Material no tiene como rol. */
@Immutable
data class SignaCoColores(
    val resaltador: Color = Resaltador,
    val cuadricula: Color = Cuadricula,
    val borde: Color = Borde,
    val correccion: Color = Correccion,
    val correccionTonal: Color = CorreccionTonal,
    val camaraAlta: Color = CamaraAlta,
    val camaraBaja: Color = CamaraBaja,
    val silueta: Color = Silueta,
    val panelCamara: Color = PanelCamara,
)

val LocalSignaCoColores = staticCompositionLocalOf { SignaCoColores() }

private fun esquemaDeColor(altoContraste: Boolean) = lightColorScheme(
    primary = Tinta,
    onPrimary = Papel,
    primaryContainer = TintaTonal,
    onPrimaryContainer = Tinta,
    secondary = TintaSuave,
    onSecondary = Papel,
    secondaryContainer = TintaTonal,
    onSecondaryContainer = Tinta,
    tertiary = Tinta,
    onTertiary = Papel,
    error = Correccion,
    onError = Papel,
    errorContainer = CorreccionTonal,
    onErrorContainer = Correccion,
    background = Papel,
    onBackground = TintaOscura,
    surface = Papel,
    onSurface = TintaOscura,
    surfaceVariant = SuperficieTonal,
    // Alto contraste: el texto secundario pasa a Tinta oscura y los contornos se oscurecen.
    onSurfaceVariant = if (altoContraste) TintaOscura else TintaSuave,
    surfaceContainerLowest = Papel,
    surfaceContainerLow = SuperficieTonal,
    surfaceContainer = SuperficieTonal,
    surfaceContainerHigh = SuperficieTonalAlta,
    surfaceContainerHighest = SuperficieTonalAlta,
    surfaceBright = Papel,
    outline = if (altoContraste) TintaSuave else Borde,
    outlineVariant = if (altoContraste) Borde else Cuadricula,
    scrim = TintaOscura,
)

/**
 * Tema «Cuaderno» de SignaCO. Solo hay esquema claro: es el único que define el diseño, y el
 * tema oscuro es una extensión de la lista de prioridades.
 *
 * Tampoco usa color dinámico de Android 12+: se arma con el fondo de pantalla del usuario y
 * no garantiza el contraste 4.5:1 que exige el RNF-10.
 *
 * @param escalaDeTexto ajuste «Tamaño del texto» de la app. Se multiplica sobre la escala de
 *        fuente del sistema, así que las dos se respetan.
 */
@Composable
fun SignaCoTheme(
    altoContraste: Boolean = false,
    escalaDeTexto: Float = 1f,
    content: @Composable () -> Unit,
) {
    val densidad = LocalDensity.current
    val colores = SignaCoColores(borde = if (altoContraste) TintaSuave else Borde)

    MaterialTheme(
        colorScheme = esquemaDeColor(altoContraste),
        typography = Typography,
    ) {
        CompositionLocalProvider(
            LocalSignaCoColores provides colores,
            LocalDensity provides Density(densidad.density, densidad.fontScale * escalaDeTexto),
            content = content,
        )
    }
}

/**
 * Con la fuente por encima del 130 % las filas con contenido a los lados apilan sus partes,
 * para que los nombres no se corten a mitad de palabra (RNF-11).
 */
val fuenteGrande: Boolean
    @Composable get() = LocalDensity.current.fontScale > ESCALA_DE_FUENTE_GRANDE

private const val ESCALA_DE_FUENTE_GRANDE = 1.3f

/** Acceso corto a los colores propios desde cualquier Composable. */
val MaterialTheme.signaCoColores: SignaCoColores
    @Composable get() = LocalSignaCoColores.current
