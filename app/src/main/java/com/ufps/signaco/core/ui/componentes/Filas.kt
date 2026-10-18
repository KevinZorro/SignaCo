package com.ufps.signaco.core.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.model.ReferenciaVisual
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.core.ui.theme.fuenteGrande

/**
 * Una lección del catálogo. La lección en curso no cambia de fondo: se distingue por el
 * cuadro amarillo de la seña actual y por la acción «Seguir».
 */
@Composable
fun FilaDeLeccion(
    titulo: String,
    descripcion: String,
    estados: List<EstadoDeCuadro>,
    descripcionDelAvance: String,
    enCurso: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.md),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Medida.areaTactil)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {}
            .padding(vertical = Espacio.md),
    ) {
        val apilado = fuenteGrande
        if (!apilado) FilaDeCuadros(estados = estados, descripcionAccesible = descripcionDelAvance)
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.xxs),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (apilado) {
                FilaDeCuadros(
                    estados = estados,
                    descripcionAccesible = descripcionDelAvance,
                    modifier = Modifier.padding(top = Espacio.xs),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Con fuente grande «Seguir» no cabe junto al título: queda solo la flecha, y la
            // descripción del avance ya anuncia que la lección está en curso.
            if (enCurso && !apilado) {
                Text(
                    text = stringResource(R.string.catalogo_seguir),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = if (enCurso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * La imagen de referencia de la seña, recortada al cuadro, sobre una superficie tonal. Si la
 * seña todavía no tiene imagen, la letra.
 */
@Composable
fun MiniaturaDeSena(
    letra: String,
    modifier: Modifier = Modifier,
    referencia: ReferenciaVisual? = null,
    lado: Dp = Medida.miniatura,
) {
    ReferenciaDeSena(
        letra = letra,
        referencia = referencia,
        estiloDeLetra = MaterialTheme.typography.headlineLarge,
        escala = ContentScale.Crop,
        modifier = modifier
            .size(lado)
            .clip(Forma.pieza)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            // Decorativa: el nombre de la seña ya está en la fila.
            .clearAndSetSemantics {},
    )
}

/**
 * Una seña en una lista (lección o diccionario): su imagen y su nombre. [insignia] va junto al
 * nombre, por ejemplo «Dominada».
 */
@Composable
fun FilaDeSena(
    letra: String,
    nombre: String,
    modifier: Modifier = Modifier,
    referencia: ReferenciaVisual? = null,
    onClick: (() -> Unit)? = null,
    insignia: @Composable (() -> Unit)? = null,
) {
    val clic = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        // Sin descripción debajo, el nombre se centra con la miniatura.
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.md),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Medida.areaTactil)
            .then(clic)
            .semantics(mergeDescendants = true) {}
            .padding(vertical = Espacio.md),
    ) {
        MiniaturaDeSena(letra = letra, referencia = referencia)
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.xxs),
            modifier = Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Espacio.sm),
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                )
                insignia?.invoke()
            }
        }
    }
}

/** Marca corta junto a un nombre. Con ícono opcional para no depender solo del color. */
@Composable
fun Insignia(
    texto: String,
    modifier: Modifier = Modifier,
    icono: Int? = null,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.xxs),
        modifier = modifier,
    ) {
        if (icono != null) {
            Icon(
                painter = painterResource(icono),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(Medida.icono),
            )
        }
        Text(text = texto, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/** Separador fino entre filas equivalentes. */
@Composable
fun Separador(modifier: Modifier = Modifier) {
    HorizontalDivider(
        thickness = Medida.trazoFino,
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun FilasPreview() {
    SignaCoTheme {
        Column(Modifier.padding(horizontal = Espacio.margen)) {
            FilaDeLeccion(
                titulo = "Vocales",
                descripcion = "A, I, O, U",
                estados = listOf(
                    EstadoDeCuadro.DOMINADA,
                    EstadoDeCuadro.EN_CURSO,
                    EstadoDeCuadro.POR_APRENDER,
                    EstadoDeCuadro.POR_APRENDER,
                ),
                descripcionDelAvance = "",
                enCurso = true,
                onClick = {},
            )
            Separador()
            // Peor caso: el nombre de lección más largo.
            FilaDeLeccion(
                titulo = "Consonantes P, Q, R, S, T, V, W, X y Y",
                descripcion = "Último grupo de consonantes, con giros de muñeca y dedos cruzados.",
                estados = List(4) { EstadoDeCuadro.POR_APRENDER },
                descripcionDelAvance = "",
                enCurso = false,
                onClick = {},
            )
            Separador()
            FilaDeSena(
                letra = "A",
                nombre = "Letra A",
                onClick = {},
                insignia = { Insignia(texto = "Dominada", icono = R.drawable.ic_check) },
            )
            Separador()
            FilaDeSena(
                letra = "Ñ",
                nombre = "Letra Ñ",
                insignia = {
                    Insignia(texto = "Fuera del MVP", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
            )
        }
    }
}
