package com.ufps.signaco.core.senas.domain

import com.ufps.signaco.core.senas.domain.model.Sena
import kotlinx.coroutines.flow.Flow

/**
 * Acceso al catalogo de senas. Vive en core porque lo consumen varias features
 * (diccionario, practica, quiz) y ninguna es su duena.
 */
interface SenaRepository {
    fun observarSenas(): Flow<List<Sena>>
    fun observarSena(senaId: String): Flow<Sena?>
}
