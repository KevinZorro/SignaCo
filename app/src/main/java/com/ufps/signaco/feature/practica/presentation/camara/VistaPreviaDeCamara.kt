package com.ufps.signaco.feature.practica.presentation.camara

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ufps.signaco.core.reconocimiento.data.DiagnosticoDeAspecto
import com.ufps.signaco.core.reconocimiento.data.RegistroDeDepuracion
import com.ufps.signaco.feature.practica.data.camara.AnalizadorDeManosMediaPipe
import com.ufps.signaco.feature.practica.domain.model.FrameDeLandmarks
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Vista previa de la cámara con el análisis de manos enganchado.
 *
 * Es el único punto de la capa de presentación que toca CameraX, y el único de toda la app que
 * toca MediaPipe junto con [AnalizadorDeManosMediaPipe]. El dominio solo recibe
 * [FrameDeLandmarks], que son 63 floats.
 *
 * No se guarda ni un frame: se analiza y se descarta (RNF-06).
 */
@Composable
fun VistaPreviaDeCamara(
    camaraFrontal: Boolean,
    activa: Boolean,
    alLlegarFrame: (FrameDeLandmarks) -> Unit,
    alFallar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexto = LocalContext.current
    val duenoDelCiclo = LocalLifecycleOwner.current
    val alLlegarFrameActual by rememberUpdatedState(alLlegarFrame)
    val alFallarActual by rememberUpdatedState(alFallar)

    val dependencias = remember(contexto) {
        EntryPointAccessors
            .fromApplication(contexto.applicationContext, DependenciasDeCamara::class.java)
    }
    val registro = dependencias.registroDeDepuracion()
    val diagnostico = dependencias.diagnosticoDeAspecto()

    // Un hilo propio para el análisis: si corriera en el principal, la vista previa se trabaría.
    val ejecutor = remember { Executors.newSingleThreadExecutor() }
    val vistaPrevia = remember { PreviewView(contexto) }

    val analizador = remember(dependencias) {
        runCatching {
            diagnostico.reiniciar()
            AnalizadorDeManosMediaPipe(contexto, registro, diagnostico) { frame ->
                alLlegarFrameActual(frame)
            }
        }.onFailure { alFallarActual() }.getOrNull()
    }

    val analisis = remember {
        ImageAnalysis.Builder()
            // Si llega un frame mientras se procesa otro, se descarta el viejo: importa la
            // latencia, no ver todos los frames.
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
    }

    LaunchedEffect(camaraFrontal, analizador) {
        if (analizador == null) return@LaunchedEffect
        runCatching {
            val proveedor = contexto.proveedorDeCamara()
            val seleccion = if (camaraFrontal) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            val previa = Preview.Builder().build()
                .also { it.setSurfaceProvider(vistaPrevia.surfaceProvider) }

            proveedor.unbindAll()
            proveedor.bindToLifecycle(duenoDelCiclo, seleccion, previa, analisis)
        }.onFailure { alFallarActual() }
    }

    // La captura se enciende y se apaga sin volver a montar la cámara (RF10).
    LaunchedEffect(activa, camaraFrontal, analizador) {
        if (activa && analizador != null) {
            analisis.setAnalyzer(ejecutor) { imagen -> analizador.analizar(imagen, camaraFrontal) }
        } else {
            analisis.clearAnalyzer()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            analisis.clearAnalyzer()
            analizador?.cerrar()
            ejecutor.shutdown()
            runCatching { ProcessCameraProvider.getInstance(contexto).get().unbindAll() }
        }
    }

    AndroidView(factory = { vistaPrevia }, modifier = modifier)
}

private suspend fun Context.proveedorDeCamara(): ProcessCameraProvider = suspendCoroutine { cont ->
    ProcessCameraProvider.getInstance(this).also { futuro ->
        futuro.addListener({ cont.resume(futuro.get()) }, ContextCompat.getMainExecutor(this))
    }
}

/**
 * El analizador no puede inyectarse en un composable, así que se toma del grafo de Hilt por aquí.
 * Es el patrón documentado para los puntos de entrada que no son ViewModel ni Activity.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface DependenciasDeCamara {
    fun registroDeDepuracion(): RegistroDeDepuracion

    /** Diagnóstico temporal de la relación de aspecto. Se quita junto con él. */
    fun diagnosticoDeAspecto(): DiagnosticoDeAspecto
}
