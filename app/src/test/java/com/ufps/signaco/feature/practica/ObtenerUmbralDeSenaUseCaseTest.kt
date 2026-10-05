package com.ufps.signaco.feature.practica

import com.ufps.signaco.core.reconocimiento.domain.ConfiguracionDeReconocimiento
import com.ufps.signaco.core.reconocimiento.domain.ProveedorDeConfiguracion
import com.ufps.signaco.feature.practica.domain.usecase.ObtenerUmbralDeSenaUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObtenerUmbralDeSenaUseCaseTest {

    private val proveedor = object : ProveedorDeConfiguracion {
        override suspend fun obtener() = ConfiguracionDeReconocimiento.RESPALDO.copy(
            umbralPorLetra = mapOf("A" to 0.04f),
            umbralGlobal = 0.1f,
        )
    }

    private val caso = ObtenerUmbralDeSenaUseCase(proveedor)

    @Test
    fun `usa el umbral propio de la letra`() = runTest {
        assertEquals(0.04f, caso("A"), 0.0001f)
    }

    @Test
    fun `sin umbral propio cae al global del config`() = runTest {
        assertEquals(0.1f, caso("B"), 0.0001f)
    }
}
