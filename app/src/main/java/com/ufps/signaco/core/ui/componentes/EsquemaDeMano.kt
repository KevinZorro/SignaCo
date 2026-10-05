package com.ufps.signaco.core.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.SignaCoTheme

/** Las conexiones entre los 21 puntos de MediaPipe HandLandmarker. */
private val CONEXIONES = listOf(
    0 to 1, 1 to 2, 2 to 3, 3 to 4,
    0 to 5, 5 to 6, 6 to 7, 7 to 8,
    5 to 9, 9 to 10, 10 to 11, 11 to 12,
    9 to 13, 13 to 14, 14 to 15, 15 to 16,
    13 to 17, 0 to 17, 17 to 18, 18 to 19, 19 to 20,
)

/** Índices de las yemas, del pulgar al meñique. */
val YEMAS = listOf(4, 8, 12, 16, 20)

/**
 * Una mano derecha abierta con la palma al frente, en coordenadas de 0 a 1 (x, y por punto).
 * Es solo ilustración: no es una seña ni sale del modelo.
 */
val MANO_DE_REFERENCIA = floatArrayOf(
    0.50f, 0.95f,
    0.36f, 0.86f, 0.26f, 0.74f, 0.18f, 0.62f, 0.11f, 0.52f,
    0.40f, 0.50f, 0.38f, 0.34f, 0.37f, 0.22f, 0.36f, 0.11f,
    0.52f, 0.48f, 0.53f, 0.31f, 0.54f, 0.18f, 0.55f, 0.06f,
    0.63f, 0.52f, 0.67f, 0.37f, 0.70f, 0.26f, 0.72f, 0.16f,
    0.73f, 0.58f, 0.79f, 0.48f, 0.83f, 0.40f, 0.87f, 0.32f,
)

/** Colores de una mano dibujada: líneas, relleno de los puntos y su contorno. */
data class EstiloDeMano(
    val linea: Color,
    val relleno: Color,
    val contorno: Color,
)

/**
 * Dibuja la mano a partir de puntos ya ubicados en el lienzo. Lo usan la ilustración y la capa
 * de puntos sobre la cámara, que hace su propio mapeo desde la imagen.
 */
fun DrawScope.dibujarMano(puntos: List<Offset>, estilo: EstiloDeMano) {
    if (puntos.size < 21) return
    val grosor = 2.dp.toPx()
    val radio = 5.dp.toPx()
    CONEXIONES.forEach { (desde, hasta) ->
        drawLine(estilo.linea, puntos[desde], puntos[hasta], grosor, StrokeCap.Round)
    }
    puntos.forEach { punto ->
        drawCircle(estilo.relleno, radio, punto)
        drawCircle(estilo.contorno, radio, punto, style = Stroke(grosor))
    }
}

/** Convierte (x, y) normalizados a puntos del lienzo, espejando si hace falta. */
fun ubicarPuntos(
    puntos: FloatArray,
    ancho: Float,
    alto: Float,
    espejar: Boolean = false,
): List<Offset> = List(puntos.size / 2) { indice ->
    val x = puntos[indice * 2]
    val y = puntos[indice * 2 + 1]
    Offset((if (espejar) 1f - x else x) * ancho, y * alto)
}

/**
 * Ilustración de la mano sobre una superficie tonal lisa (la cuadrícula está reservada al
 * cuaderno). Con [etiquetas] escribe el nombre de cada dedo unido a su yema por una línea fina.
 *
 * @param descripcion lo que TalkBack lee: el dibujo no es legible por sí mismo.
 * @param espejar dibuja la mano izquierda, según el ajuste de mano dominante.
 * @param aLaDerecha corre la mano al borde derecho para dejar sitio a un texto a la izquierda.
 */
@Composable
fun EsquemaDeMano(
    descripcion: String,
    modifier: Modifier = Modifier,
    puntos: FloatArray = MANO_DE_REFERENCIA,
    espejar: Boolean = false,
    etiquetas: List<String> = emptyList(),
    aLaDerecha: Boolean = false,
) {
    val estilo = EstiloDeMano(
        linea = MaterialTheme.colorScheme.primary,
        relleno = MaterialTheme.colorScheme.surface,
        contorno = MaterialTheme.colorScheme.primary,
    )
    val colorDeEtiqueta = MaterialTheme.colorScheme.onSurfaceVariant
    val estiloDeEtiqueta = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface)
    val medidor = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(Forma.contenedor)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(Espacio.lg)
            .semantics { contentDescription = descripcion },
    ) {
        // La mano ocupa un cuadrado centrado para no deformarse con el ancho. Con etiquetas se
        // achica y baja un poco para dejar sitio a los nombres encima de los dedos.
        val reduccion = if (etiquetas.isEmpty()) 1f else ESCALA_CON_ETIQUETAS
        val lado = size.minDimension * reduccion
        val izquierda = if (aLaDerecha) size.width - lado else (size.width - lado) / 2f
        val origen = Offset(izquierda, size.height - lado)
        val ubicados = ubicarPuntos(puntos, lado, lado, espejar).map { it + origen }
        dibujarMano(ubicados, estilo)

        etiquetas.take(YEMAS.size).forEachIndexed { dedo, nombre ->
            dibujarEtiqueta(
                yema = ubicados[YEMAS[dedo]],
                nombre = nombre,
                direccion = when {
                    dedo == DEDO_MEDIO -> 0
                    (dedo < DEDO_MEDIO) != espejar -> -1
                    else -> 1
                },
                color = colorDeEtiqueta,
                medir = { medidor.measure(it, estiloDeEtiqueta) },
            )
        }
    }
}

/** @param direccion -1 lleva el nombre arriba a la izquierda, 0 arriba y 1 arriba a la derecha. */
private fun DrawScope.dibujarEtiqueta(
    yema: Offset,
    nombre: String,
    direccion: Int,
    color: Color,
    medir: (String) -> TextLayoutResult,
) {
    val texto = medir(nombre)
    val separacion = 16.dp.toPx()
    val ancla = Offset(yema.x + direccion * separacion, yema.y - separacion)
    drawLine(color, yema, ancla, 1.dp.toPx())
    val x = when (direccion) {
        -1 -> ancla.x - texto.size.width
        0 -> ancla.x - texto.size.width / 2f
        else -> ancla.x
    }
    val y = ancla.y - texto.size.height
    val maximoX = (size.width - texto.size.width).coerceAtLeast(0f)
    drawText(texto, topLeft = Offset(x.coerceIn(0f, maximoX), y.coerceAtLeast(0f)))
}

private const val DEDO_MEDIO = 2
private const val ESCALA_CON_ETIQUETAS = 0.82f

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun EsquemaDeManoPreview() {
    SignaCoTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.md),
            modifier = Modifier.padding(Espacio.margen),
        ) {
            EsquemaDeMano(descripcion = "")
            EsquemaDeMano(
                descripcion = "",
                etiquetas = listOf("Pulgar", "Índice", "Medio", "Anular", "Meñique"),
            )
        }
    }
}
