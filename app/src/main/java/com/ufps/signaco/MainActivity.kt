package com.ufps.signaco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ufps.signaco.core.navegacion.SignaCoApp
import com.ufps.signaco.core.ui.theme.SignaCoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Única Activity de la app. Toda la navegación ocurre en Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignaCoTheme {
                SignaCoApp()
            }
        }
    }
}
