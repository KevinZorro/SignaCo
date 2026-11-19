package com.ufps.signaco.feature.practica

import com.ufps.signaco.core.reconocimiento.domain.Alfabeto
import com.ufps.signaco.core.reconocimiento.domain.ConfiguracionDeReconocimiento
import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import com.ufps.signaco.feature.practica.domain.usecase.CrearEstabilizadorUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CrearEstabilizadorUseCaseTest {

    @Test
    fun `crea estabilizador con valores por defecto de la configuracion`() = runTest {
        val proveedor = object : ProveedorDeConfiguracion {
            override suspend fun obtener(): ConfiguracionDeReconocimiento =
                ConfiguracionDeReconocimiento.RESPALDO
        }
        val caso = CrearEstabilizadorUseCase(proveedor)
        val estabilizador = caso()

        assertEquals(ConfiguracionDeReconocimiento.K_POR_DEFECTO, estabilizador.k)
        assertEquals(ConfiguracionDeReconocimiento.VENTANA_MS_POR_DEFECTO, estabilizador.ventanaMs)
        assertEquals(ConfiguracionDeReconocimiento.FRACCION_MINIMA_POR_DEFECTO, estabilizador.fraccionMinima, 0.0001f)
    }

    @Test
    fun `crea estabilizador con valores distintos a los por defecto`() = runTest {
        val configPersonalizada = ConfiguracionDeReconocimiento(
            etiquetas = Alfabeto.LETRAS,
            indiceDeRechazo = Alfabeto.LETRAS.size,
            umbralPorLetra = emptyMap(),
            umbralGlobal = 0.5f,
            k = 5,
            ventanaMs = 1500L,
            fraccionMinima = 0.75f,
        )
        val proveedor = object : ProveedorDeConfiguracion {
            override suspend fun obtener(): ConfiguracionDeReconocimiento = configPersonalizada
        }
        val caso = CrearEstabilizadorUseCase(proveedor)
        val estabilizador = caso()

        assertEquals(5, estabilizador.k)
        assertEquals(1500L, estabilizador.ventanaMs)
        assertEquals(0.75f, estabilizador.fraccionMinima, 0.0001f)
    }
}
