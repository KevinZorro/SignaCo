package com.ufps.signaco.core.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.Forma
import com.ufps.signaco.core.ui.theme.Medida
import com.ufps.signaco.core.ui.theme.SignaCoTheme

/** Título de un grupo de ajustes («Práctica», «Accesibilidad»). */
@Composable
fun EncabezadoDeGrupo(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .padding(bottom = Espacio.xs)
            .semantics { heading() },
    )
}

/** Agrupa filas sobre una superficie tonal, sin borde. Las filas se separan con [Separador]. */
@Composable
fun GrupoDeAjustes(
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Forma.contenedor)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        content = contenido,
    )
}

/** Fila con interruptor. Toda la fila es el control, no solo el interruptor. */
@Composable
fun FilaDeInterruptor(
    titulo: String,
    descripcion: String,
    activo: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.md),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Medida.areaTactil)
            .toggleable(value = activo, role = Role.Switch, onValueChange = onCambio)
            .padding(Espacio.md),
    ) {
        TextosDeFila(titulo, descripcion, Modifier.weight(1f))
        Switch(
            checked = activo,
            // El toggleable de la fila ya maneja el clic y la semántica.
            onCheckedChange = null,
            colors = SwitchDefaults.colors(uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        )
    }
}

/** Fila que abre otra pantalla. */
@Composable
fun FilaDeNavegacion(
    titulo: String,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.md),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Medida.areaTactil)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Espacio.md),
    ) {
        TextosDeFila(titulo, descripcion, Modifier.weight(1f))
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Fila con un selector de opciones excluyentes (SegmentedButton de Material 3, sin el
 * contorno grueso).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilaDeSelector(
    titulo: String,
    opciones: List<T>,
    seleccionada: T,
    etiqueta: @Composable (T) -> String,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    descripcion: String? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.sm),
        modifier = modifier
            .fillMaxWidth()
            .padding(Espacio.md),
    ) {
        TextosDeFila(titulo, descripcion)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            opciones.forEachIndexed { indice, opcion ->
                SegmentedButton(
                    selected = opcion == seleccionada,
                    onClick = { onSeleccion(opcion) },
                    shape = SegmentedButtonDefaults.itemShape(indice, opciones.size),
                    border = SegmentedButtonDefaults.borderStroke(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        width = Medida.trazoFino,
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.primary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.heightIn(min = Medida.areaTactil),
                    label = { Text(etiqueta(opcion), style = MaterialTheme.typography.labelMedium) },
                )
            }
        }
    }
}

@Composable
private fun TextosDeFila(titulo: String, descripcion: String?, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(Espacio.xxs), modifier = modifier) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (descripcion != null) {
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Confirmación de una acción irreversible. El rojo de Corrección aparece solo aquí, en la
 * acción que destruye.
 */
@Composable
fun DialogoDeConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    textoCancelar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(mensaje, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            BotonDeTexto(
                texto = textoConfirmar,
                onClick = onConfirmar,
                color = MaterialTheme.colorScheme.error,
            )
        },
        dismissButton = { BotonDeTexto(texto = textoCancelar, onClick = onCancelar) },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AjustesPreview() {
    SignaCoTheme {
        Column(Modifier.padding(Espacio.margen)) {
            EncabezadoDeGrupo("Práctica")
            GrupoDeAjustes {
                FilaDeSelector(
                    titulo = "Mano dominante",
                    descripcion = "La cámara y los clips se ajustan a esta mano.",
                    opciones = listOf("Derecha", "Izquierda"),
                    seleccionada = "Derecha",
                    etiqueta = { it },
                    onSeleccion = {},
                )
                Separador()
                FilaDeInterruptor(
                    titulo = "Vibración al acertar",
                    descripcion = "Un toque corto al confirmar la seña.",
                    activo = true,
                    onCambio = {},
                )
                Separador()
                FilaDeNavegacion(
                    titulo = "Tamaño y contraste",
                    descripcion = "Texto grande · Alto contraste",
                    onClick = {},
                )
            }
        }
    }
}
