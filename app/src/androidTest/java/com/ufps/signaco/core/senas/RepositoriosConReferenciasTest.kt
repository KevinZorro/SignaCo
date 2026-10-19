package com.ufps.signaco.core.senas

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ufps.signaco.R
import com.ufps.signaco.core.database.CatalogoInicial
import com.ufps.signaco.core.database.SignaCoDatabase
import com.ufps.signaco.core.database.baseConCatalogo
import com.ufps.signaco.core.senas.data.ReferenciasEnRecursos
import com.ufps.signaco.core.senas.data.SenaRepositoryRoom
import com.ufps.signaco.core.senas.domain.model.ReferenciaVisual
import com.ufps.signaco.feature.lecciones.data.LeccionRepositoryRoom
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Los repositorios afectados por RF02 y la migración 3, contra un SQLite de verdad. */
@RunWith(AndroidJUnit4::class)
class RepositoriosConReferenciasTest {

    private lateinit var base: SignaCoDatabase
    private val referencias = ReferenciasEnRecursos()

    @Before
    fun crear() {
        base = baseConCatalogo()
    }

    @After
    fun cerrar() = base.close()

    @Test
    fun unaSenaActivaTraeSuImagenDesdeLaBase() = runBlocking {
        val sena = SenaRepositoryRoom(base.senaDao(), referencias).observarSena("A").first()

        assertNotNull(sena)
        assertEquals(ReferenciaVisual.Imagen(R.drawable.sena_a), sena!!.referencia)
    }

    @Test
    fun unaSenaSinImagenLlegaSinReferenciaYSinFallar() = runBlocking {
        val sena = SenaRepositoryRoom(base.senaDao(), referencias).observarSena("C").first()

        assertNotNull(sena)
        assertNull(sena!!.referencia)
    }

    @Test
    fun elCatalogoCompletoSeLeeSinLaColumnaDescripcion() = runBlocking {
        val senas = SenaRepositoryRoom(base.senaDao(), referencias).observarSenas().first()

        assertEquals(CatalogoInicial.SENAS.size, senas.size)
    }

    @Test
    fun lasLetrasDeCadaLeccionTienenImagen() = runBlocking {
        val lecciones = LeccionRepositoryRoom(base.leccionDao(), referencias).observarLecciones().first()

        assertEquals(CatalogoInicial.LECCIONES.size, lecciones.size)
        lecciones.flatMap { it.senas }.filter { it.disponible }.forEach { sena ->
            assertTrue("${sena.id} sin imagen", sena.tieneReferencia)
        }
    }
}
