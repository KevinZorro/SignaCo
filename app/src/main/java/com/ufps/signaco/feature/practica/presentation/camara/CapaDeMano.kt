package com.ufps.signaco.feature.practica.presentation.camara

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.ufps.signaco.core.ui.componentes.EstiloDeMano
import com.ufps.signaco.core.ui.componentes.dibujarMano

/**
 * Dibuja los 21 puntos de MediaPipe encima de la vista previa de CameraX.
 *
 * Los puntos llegan en coordenadas 0..1 de la imagen que vio MediaPipe, ya rotada y, con la
 * cámara frontal, ya espejada igual que la vista previa. Solo falta repetir el recorte de
 * PreviewView (FILL_CENTER): la imagen se escala hasta cubrir la vista y se centra.
 *
 * @param puntos 42 floats (x, y por punto). Vacío si no hay mano.
 * @param proporcionDeImagen ancho entre alto de esa imagen.
 */
@Composable
fun CapaDeMano(
    puntos: FloatArray,
    proporcionDeImagen: Float,
    estilo: EstiloDeMano,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        if (puntos.isEmpty() || proporcionDeImagen <= 0f) return@Canvas
        val imagen = tamanoCubriendo(size, proporcionDeImagen)
        val desplazamiento = Offset((size.width - imagen.width) / 2f, (size.height - imagen.height) / 2f)
        val ubicados = List(puntos.size / 2) { indice ->
            Offset(
                desplazamiento.x + puntos[indice * 2] * imagen.width,
                desplazamiento.y + puntos[indice * 2 + 1] * imagen.height,
            )
        }
        dibujarMano(ubicados, estilo)
    }
}

/** Tamaño de la imagen escalada para cubrir [vista] sin deformarse, como hace FILL_CENTER. */
private fun tamanoCubriendo(vista: Size, proporcion: Float): Size =
    if (vista.width / vista.height > proporcion) {
        Size(vista.width, vista.width / proporcion)
    } else {
        Size(vista.height * proporcion, vista.height)
    }
