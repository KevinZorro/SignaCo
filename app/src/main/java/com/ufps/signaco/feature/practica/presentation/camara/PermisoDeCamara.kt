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
 * Prepara la solicitud del permiso de cámara y devuelve la función que la lanza.
 *
 * El diálogo del sistema ya no sale solo al entrar: antes se explica para qué hace falta la
 * cámara y la persona decide con «Permitir cámara». Si el permiso ya estaba concedido, se avisa
 * de inmediato y la explicación no se muestra.
 *
 * Si el usuario lo deniega no se cierra nada (RNF-13): la pantalla lo dice y deja volver a
 * pedirlo. Cuando el sistema ya no muestra el diálogo por haberlo denegado dos veces, queda el
 * camino de los ajustes de la app.
 */
@Composable
fun rememberSolicitudDePermisoDeCamara(alResponder: (Boolean) -> Unit): () -> Unit {
    val contexto = LocalContext.current
    val yaConcedido = remember(contexto) { contexto.tienePermisoDeCamara() }

    val lanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = alResponder,
    )

    LaunchedEffect(yaConcedido) {
        if (yaConcedido) alResponder(true)
    }
    return { lanzador.launch(Manifest.permission.CAMERA) }
}

fun Context.tienePermisoDeCamara(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
