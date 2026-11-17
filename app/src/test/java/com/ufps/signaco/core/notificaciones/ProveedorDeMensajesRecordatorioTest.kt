package com.ufps.signaco.core.notificaciones

import com.ufps.signaco.core.notificaciones.domain.ProveedorDeMensajesRecordatorioAleatorio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ProveedorDeMensajesRecordatorioTest {

    @Test
    fun `provee mensajes con titulo y cuerpo validos y no vacios`() {
        val proveedor = ProveedorDeMensajesRecordatorioAleatorio()

        repeat(20) {
            val mensaje = proveedor.obtenerMensaje()
            assertEquals("SignaCO", mensaje.titulo)
            assertFalse(mensaje.cuerpo.isBlank())
            assertTrue(mensaje.cuerpo.length > 10)
        }
    }

    @Test
    fun `rota entre las distintas variaciones de recordatorio`() {
        val mensajesObtenidos = mutableSetOf<String>()
        val proveedor = ProveedorDeMensajesRecordatorioAleatorio()

        repeat(50) {
            mensajesObtenidos.add(proveedor.obtenerMensaje().cuerpo)
        }

        // Debe haber más de 1 variación encontrada
        assertTrue(mensajesObtenidos.size > 1)
    }
}
