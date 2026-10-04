package com.ufps.signaco.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ufps.signaco.R

/**
 * Atkinson Hyperlegible Next va empaquetada en res/font porque la app no usa internet.
 * Es una fuente variable: cada peso fija el eje wght (soportado desde API 26, el minSdk).
 */
@OptIn(ExperimentalTextApi::class)
private fun pesoDeAtkinson(peso: Int) = Font(
    resId = R.font.atkinson_hyperlegible_next,
    weight = FontWeight(peso),
    variationSettings = FontVariation.Settings(FontVariation.weight(peso)),
)

val AtkinsonHyperlegibleNext = FontFamily(
    pesoDeAtkinson(400),
    pesoDeAtkinson(700),
    pesoDeAtkinson(800),
)

private fun estilo(
    tamano: Int,
    peso: Int,
    interlineado: Float,
    espaciado: TextUnit = 0.sp,
) = TextStyle(
    fontFamily = AtkinsonHyperlegibleNext,
    fontWeight = FontWeight(peso),
    fontSize = tamano.sp,
    lineHeight = interlineado.em,
    letterSpacing = espaciado,
)

/** Puntaje del quiz. Es el único estilo que no tiene rol en Material. */
val EstiloNumero = estilo(tamano = 120, peso = 800, interlineado = 1.0f, espaciado = (-3).sp)

/**
 * Escala «SignaCO/…» de Figma mapeada a los roles de Material. Los tamaños van en sp para que
 * respeten el escalado de fuente del sistema (RNF-11): nunca usar dp para texto.
 */
private val Palabra = estilo(tamano = 48, peso = 800, interlineado = 1.05f, espaciado = (-1).sp)
private val Titulo = estilo(tamano = 36, peso = 800, interlineado = 1.1f, espaciado = (-1).sp)
private val Feedback = estilo(tamano = 24, peso = 800, interlineado = 1.2f)
private val Seccion = estilo(tamano = 21, peso = 700, interlineado = 1.25f)
private val Cuerpo = estilo(tamano = 16, peso = 400, interlineado = 1.45f)
private val CuerpoFuerte = estilo(tamano = 16, peso = 700, interlineado = 1.3f)
private val Secundario = estilo(tamano = 14, peso = 400, interlineado = 1.4f)
private val SecundarioFuerte = estilo(tamano = 14, peso = 700, interlineado = 1.4f)
private val Boton = estilo(tamano = 18, peso = 700, interlineado = 1.2f)
private val Leyenda = estilo(tamano = 12, peso = 700, interlineado = 1.35f)
private val Nota = estilo(tamano = 12, peso = 400, interlineado = 1.35f)

/**
 * Escala «SignaCO/…» de Figma mapeada a los roles de Material. Todos los roles apuntan a un
 * estilo de Figma para que ningún texto caiga en Roboto. Los tamaños van en sp para que
 * respeten el escalado de fuente del sistema (RNF-11): nunca usar dp para texto.
 */
val Typography = Typography(
    displayLarge = Palabra,
    displayMedium = Palabra,
    displaySmall = Palabra,
    headlineLarge = Titulo,
    headlineMedium = Titulo,
    headlineSmall = Feedback,
    titleLarge = Seccion,
    titleMedium = CuerpoFuerte,
    titleSmall = SecundarioFuerte,
    bodyLarge = Cuerpo,
    bodyMedium = Secundario,
    bodySmall = Nota,
    labelLarge = Boton,
    labelMedium = SecundarioFuerte,
    labelSmall = Leyenda,
)
