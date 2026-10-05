package com.ufps.signaco

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ufps.signaco.core.navegacion.SignaCoApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Única Activity de la app. Toda la navegación ocurre en Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // La app solo tiene tema claro: los íconos de las barras del sistema van siempre oscuros,
        // aunque el teléfono esté en modo oscuro.
        val barrasClaras = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = barrasClaras, navigationBarStyle = barrasClaras)
        // El tema lo aplica SignaCoApp, porque depende de los ajustes de tamaño y contraste.
        setContent {
            SignaCoApp()
        }
    }
}
