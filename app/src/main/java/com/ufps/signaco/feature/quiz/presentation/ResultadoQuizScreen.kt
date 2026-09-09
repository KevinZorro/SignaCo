package com.ufps.signaco.feature.quiz.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.componentes.BarraDeAvance
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import com.ufps.signaco.feature.quiz.domain.model.ResultadoQuiz
import kotlin.math.roundToInt

/**
 * Pantalla de solo lectura: recibe el puntaje como argumento de navegación, así que no
 * necesita ViewModel. Guardar el resultado ya lo hizo el quiz.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadoQuizScreen(
    aciertos: Int,
    total: Int,
    onRepetirQuiz: () -> Unit,
    onVolverALecciones: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resultado = ResultadoQuiz(aciertos = aciertos, total = total)
    val porcentaje = (resultado.porcentaje * 100).roundToInt()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.resultado_titulo)) }) },
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = if (resultado.aprobado) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Refresh
                },
                contentDescription = null,
                tint = if (resultado.aprobado) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = if (resultado.aprobado) {
                    stringResource(R.string.resultado_aprobado)
                } else {
                    stringResource(R.string.resultado_reprobado)
                },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = stringResource(R.string.resultado_puntaje, aciertos, total),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            BarraDeAvance(
                porcentaje = resultado.porcentaje,
                descripcionAccesible = stringResource(R.string.comun_porcentaje, porcentaje),
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = if (resultado.aprobado) {
                    stringResource(R.string.resultado_detalle_aprobado)
                } else {
                    stringResource(R.string.resultado_detalle_reprobado)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
            Button(
                onClick = onVolverALecciones,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(top = 32.dp),
            ) {
                Text(stringResource(R.string.resultado_volver_lecciones))
            }
            OutlinedButton(
                onClick = onRepetirQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(top = 12.dp),
            ) {
                Text(stringResource(R.string.resultado_repetir))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultadoQuizPreview() {
    SignaCoTheme {
        ResultadoQuizScreen(
            aciertos = 4,
            total = 5,
            onRepetirQuiz = {},
            onVolverALecciones = {},
        )
    }
}
