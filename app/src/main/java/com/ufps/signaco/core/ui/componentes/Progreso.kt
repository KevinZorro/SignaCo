package com.ufps.signaco.core.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.core.ui.theme.signaCoColores

/** Los tres estados que puede tener una seña en el cuaderno. */
enum class EstadoDeCuadro { DOMINADA, EN_CURSO, POR_APRENDER }

/**
 * Un cuadro del cuaderno. El amarillo marca lo que se aprende ahora, pero nunca va solo: el
 * cuadro en curso lleva contorno y la leyenda lo nombra (RNF-09).
 */
@Composable
fun CuadroDeProgreso(
    estado: EstadoDeCuadro,
    modifier: Modifier = Modifier,
    lado: Dp = Medida.cuadroDeProgreso,
) {
    val colores = MaterialTheme.signaCoColores
    val base = modifier
        .size(lado)
        .clip(Forma.pieza)
    val cuadro = when (estado) {
        EstadoDeCuadro.DOMINADA -> base.background(MaterialTheme.colorScheme.primary)
        EstadoDeCuadro.EN_CURSO -> base
            .background(colores.resaltador)
            .border(Medida.trazoFino, MaterialTheme.colorScheme.onSurface, Forma.pieza)
        EstadoDeCuadro.POR_APRENDER -> base
            .background(MaterialTheme.colorScheme.surface)
            .border(Medida.trazoFino, colores.borde, Forma.pieza)
    }
    Box(cuadro)
}

/** Fila de cuadros, uno por seña. TalkBack la lee como un resumen, no cuadro por cuadro. */
@Composable
fun FilaDeCuadros(
    estados: List<EstadoDeCuadro>,
    descripcionAccesible: String,
    modifier: Modifier = Modifier,
    lado: Dp = Medida.cuadroDeProgreso,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Espacio.xxs),
        modifier = modifier.clearAndSetSemantics { contentDescription = descripcionAccesible },
    ) {
        estados.forEach { CuadroDeProgreso(estado = it, lado = lado) }
    }
}

/** Leyenda de los tres estados del cuaderno. */
@Composable
fun LeyendaDeProgreso(
    modifier: Modifier = Modifier,
    etiquetaEnCurso: String = stringResource(R.string.leyenda_en_curso),
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Espacio.md),
        verticalArrangement = Arrangement.spacedBy(Espacio.xs),
        modifier = modifier,
    ) {
        ElementoDeLeyenda(EstadoDeCuadro.DOMINADA, stringResource(R.string.leyenda_dominada))
        ElementoDeLeyenda(EstadoDeCuadro.EN_CURSO, etiquetaEnCurso)
        ElementoDeLeyenda(EstadoDeCuadro.POR_APRENDER, stringResource(R.string.leyenda_por_aprender))
    }
}

@Composable
private fun ElementoDeLeyenda(estado: EstadoDeCuadro, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.xs),
    ) {
        CuadroDeProgreso(estado)
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Progreso de la sesión de práctica, un segmento por seña. Solo el segmento actual lleva
 * amarillo; los terminados van en Tinta.
 */
@Composable
fun ProgresoSegmentado(
    total: Int,
    actual: Int,
    descripcionAccesible: String,
    modifier: Modifier = Modifier,
) {
    val colores = MaterialTheme.signaCoColores
    Row(
        horizontalArrangement = Arrangement.spacedBy(Espacio.xs),
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = descripcionAccesible },
    ) {
        repeat(total) { indice ->
            val color = when {
                indice < actual -> MaterialTheme.colorScheme.primary
                indice == actual -> colores.resaltador
                else -> colores.cuadricula
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(Medida.segmentoDeProgreso)
                    .clip(Forma.pildora)
                    .background(color),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ProgresoPreview() {
    SignaCoTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.md),
            modifier = Modifier.padding(Espacio.margen),
        ) {
            ProgresoSegmentado(total = 4, actual = 1, descripcionAccesible = "")
            FilaDeCuadros(
                estados = listOf(
                    EstadoDeCuadro.DOMINADA,
                    EstadoDeCuadro.EN_CURSO,
                    EstadoDeCuadro.POR_APRENDER,
                    EstadoDeCuadro.POR_APRENDER,
                ),
                descripcionAccesible = "",
            )
            FilaDeCuadros(
                estados = List(4) { EstadoDeCuadro.DOMINADA },
                descripcionAccesible = "",
                lado = Medida.areaTactil,
            )
            LeyendaDeProgreso()
        }
    }
}
