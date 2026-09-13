package com.ufps.signaco.feature.practica.presentation.camara

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Pide el permiso de cámara una sola vez al entrar y avisa del resultado.
 *
 * Si el usuario lo deniega no se cierra nada (RNF-13): la pantalla muestra un mensaje que
 * explica para qué hace falta y un botón para volver a pedirlo. Cuando el sistema ya no muestra
 * el diálogo por haberlo denegado dos veces, ese botón lleva a los ajustes de la app.
 */
@Composable
fun SolicitarPermisoDeCamara(
    alResponder: (Boolean) -> Unit,
    solicitar: Boolean,
) {
    val contexto = LocalContext.current
    val yaConcedido = remember(contexto) { contexto.tienePermisoDeCamara() }

    val lanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = alResponder,
    )

    LaunchedEffect(solicitar, yaConcedido) {
        when {
            yaConcedido -> alResponder(true)
            solicitar -> lanzador.launch(Manifest.permission.CAMERA)
        }
    }
}

fun Context.tienePermisoDeCamara(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
