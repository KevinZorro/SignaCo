package com.ufps.signaco.core.ui.componentes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Barra de avance con descripción para TalkBack: el porcentaje no puede quedar solo en el
 * color ni en el largo de la barra (RNF-09).
 */
@Composable
fun BarraDeAvance(
    porcentaje: Float,
    descripcionAccesible: String,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        progress = { porcentaje.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .semantics { contentDescription = descripcionAccesible },
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}
