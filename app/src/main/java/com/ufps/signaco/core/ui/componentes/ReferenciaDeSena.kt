package com.ufps.signaco.core.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.ufps.signaco.core.senas.domain.model.ReferenciaVisual

/**
 * Cómo se hace una seña (RF02): su imagen de referencia o, si todavía no tiene, la letra grande.
 *
 * No lleva descripción accesible propia: la pone quien la usa, que sabe si es decorativa (una
 * miniatura junto al nombre) o el contenido principal (el recuadro «Practica conmigo»).
 *
 * Cuando haya fotos o video, se agrega aquí el caso nuevo de [ReferenciaVisual] y ninguna
 * pantalla cambia.
 */
@Composable
fun ReferenciaDeSena(
    letra: String,
    referencia: ReferenciaVisual?,
    estiloDeLetra: TextStyle,
    modifier: Modifier = Modifier,
    escala: ContentScale = ContentScale.Fit,
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        when (referencia) {
            is ReferenciaVisual.Imagen -> Image(
                painter = painterResource(referencia.recurso),
                contentDescription = null,
                contentScale = escala,
                modifier = Modifier.fillMaxSize(),
            )

            null -> Text(
                text = letra,
                style = estiloDeLetra,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
