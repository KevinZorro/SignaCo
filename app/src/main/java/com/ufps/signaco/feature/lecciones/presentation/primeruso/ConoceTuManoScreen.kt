package com.ufps.signaco.feature.lecciones.presentation.primeruso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.componentes.BotonPrincipal
import com.ufps.signaco.core.ui.componentes.EsquemaDeMano
import com.ufps.signaco.core.ui.theme.Espacio
import com.ufps.signaco.core.ui.theme.SignaCoTheme

/** Primer arranque: los nombres de los dedos, que las indicaciones usan todo el tiempo. */
@Composable
fun ConoceTuManoScreen(
    onContinuar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConoceTuManoViewModel = hiltViewModel(),
) {
    val manoIzquierda by viewModel.manoIzquierda.collectAsStateWithLifecycle()
    ConoceTuManoContenido(
        manoIzquierda = manoIzquierda,
        onContinuar = { viewModel.continuar(onContinuar) },
        modifier = modifier,
    )
}

@Composable
fun ConoceTuManoContenido(
    manoIzquierda: Boolean,
    onContinuar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Espacio.lg),
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Espacio.margen, vertical = Espacio.lg),
        ) {
            Text(
                text = stringResource(R.string.primer_uso_titulo),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(top = Espacio.xl)
                    .semantics { heading() },
            )
            EsquemaDeMano(
                descripcion = stringResource(R.string.cd_esquema_dedos),
                espejar = manoIzquierda,
                etiquetas = listOf(
                    stringResource(R.string.dedo_pulgar),
                    stringResource(R.string.dedo_indice),
                    stringResource(R.string.dedo_medio),
                    stringResource(R.string.dedo_anular),
                    stringResource(R.string.dedo_menique),
                ),
            )
            Text(
                text = stringResource(R.string.primer_uso_explicacion),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            BotonPrincipal(
                texto = stringResource(R.string.primer_uso_continuar),
                onClick = onContinuar,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ConoceTuManoPreview() {
    SignaCoTheme {
        ConoceTuManoContenido(manoIzquierda = false, onContinuar = {})
    }
}
