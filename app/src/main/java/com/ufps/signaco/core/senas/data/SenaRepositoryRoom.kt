package com.ufps.signaco.core.senas.data

import com.ufps.signaco.core.database.dao.SenaDao
import com.ufps.signaco.core.senas.domain.SenaRepository
import com.ufps.signaco.core.senas.domain.model.Sena
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SenaRepositoryRoom @Inject constructor(
    private val senaDao: SenaDao,
) : SenaRepository {

    override fun observarSenas(): Flow<List<Sena>> =
        senaDao.observarSenas().map { filas -> filas.map { it.aDominio() } }

    override fun observarSena(senaId: String): Flow<Sena?> =
        senaDao.observarSena(senaId).map { it?.aDominio() }
}
