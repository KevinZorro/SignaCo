package com.ufps.signaco.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = SobreAzulPrimario,
    primaryContainer = ContenedorAzul,
    onPrimaryContainer = SobreContenedorAzul,
    secondary = GrisAzulSecundario,
    onSecondary = SobreGrisAzulSecundario,
    secondaryContainer = ContenedorGrisAzul,
    onSecondaryContainer = SobreContenedorGrisAzul,
    tertiary = VerdeTerciario,
    onTertiary = SobreVerdeTerciario,
    tertiaryContainer = ContenedorVerde,
    onTertiaryContainer = SobreContenedorVerde,
    error = RojoError,
    onError = SobreRojoError,
    errorContainer = ContenedorRojo,
    onErrorContainer = SobreContenedorRojo,
    background = FondoClaro,
    onBackground = SobreFondoClaro,
    surface = FondoClaro,
    onSurface = SobreFondoClaro,
    surfaceVariant = VarianteSuperficieClara,
    onSurfaceVariant = SobreVarianteSuperficieClara,
    outline = ContornoClaro,
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulPrimarioOscuro,
    onPrimary = SobreAzulPrimarioOscuro,
    primaryContainer = ContenedorAzulOscuro,
    onPrimaryContainer = SobreContenedorAzulOscuro,
    secondary = GrisAzulSecundarioOscuro,
    onSecondary = SobreGrisAzulSecundarioOscuro,
    secondaryContainer = ContenedorGrisAzulOscuro,
    onSecondaryContainer = SobreContenedorGrisAzulOscuro,
    tertiary = VerdeTerciarioOscuro,
    onTertiary = SobreVerdeTerciarioOscuro,
    tertiaryContainer = ContenedorVerdeOscuro,
    onTertiaryContainer = SobreContenedorVerdeOscuro,
    error = RojoErrorOscuro,
    onError = SobreRojoErrorOscuro,
    errorContainer = ContenedorRojoOscuro,
    onErrorContainer = SobreContenedorRojoOscuro,
    background = FondoOscuro,
    onBackground = SobreFondoOscuro,
    surface = FondoOscuro,
    onSurface = SobreFondoOscuro,
    surfaceVariant = VarianteSuperficieOscura,
    onSurfaceVariant = SobreVarianteSuperficieOscura,
    outline = ContornoOscuro,
)

/**
 * Tema de la app.
 *
 * No usa color dinámico de Android 12+ a propósito: el color dinámico se arma con el fondo
 * de pantalla del usuario y no garantiza el contraste 4.5:1 que exige el RNF-10. La paleta
 * fija sí está medida.
 */
@Composable
fun SignaCoTheme(
    temaOscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (temaOscuro) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content,
    )
}
