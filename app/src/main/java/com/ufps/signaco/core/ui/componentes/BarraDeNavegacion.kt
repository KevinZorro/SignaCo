package com.ufps.signaco.core.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.ufps.signaco.R
import com.ufps.signaco.core.ui.theme.SignaCoTheme

data class ElementoDeNavegacion(
    val etiqueta: String,
    @DrawableRes val icono: Int,
    val seleccionado: Boolean,
    val onClick: () -> Unit,
)

/**
 * Barra inferior de Material 3. La pestaña activa se marca con una superficie tonal de Tinta:
 * el amarillo queda para lo que se aprende, no para la navegación.
 */
@Composable
fun BarraDeNavegacion(
    elementos: List<ElementoDeNavegacion>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Separador()
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            elementos.forEach { elemento ->
                NavigationBarItem(
                    selected = elemento.seleccionado,
                    onClick = elemento.onClick,
                    icon = {
                        Icon(
                            painter = painterResource(elemento.icono),
                            // La etiqueta siempre está visible: describirla otra vez sería
                            // redundante para TalkBack.
                            contentDescription = null,
                        )
                    },
                    label = { Text(elemento.etiqueta, style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BarraDeNavegacionPreview() {
    SignaCoTheme {
        BarraDeNavegacion(
            elementos = listOf(
                ElementoDeNavegacion("Aprender", R.drawable.ic_menu_book, true) {},
                ElementoDeNavegacion("Diccionario", R.drawable.ic_book_2, false) {},
                ElementoDeNavegacion("Progreso", R.drawable.ic_grid_view, false) {},
            ),
        )
    }
}
