package com.ufps.signaco.feature.progreso.data

import com.ufps.signaco.core.database.dao.RachaDao
import com.ufps.signaco.core.database.entity.RachaEntity
import com.ufps.signaco.feature.progreso.domain.RachaRepository
import com.ufps.signaco.feature.progreso.domain.model.Racha
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RachaRepositoryRoom @Inject constructor(
    private val rachaDao: RachaDao,
) : RachaRepository {

    override fun observarRacha(): Flow<Racha> =
        rachaDao.observarRacha(RachaEntity.FILA_UNICA).map { it?.aDominio() ?: Racha.INICIAL }

    override suspend fun obtenerRacha(): Racha =
        rachaDao.obtenerRacha(RachaEntity.FILA_UNICA)?.aDominio() ?: Racha.INICIAL

    override suspend fun guardarRacha(racha: Racha) {
        rachaDao.guardarRacha(
            RachaEntity(
                id = RachaEntity.FILA_UNICA,
                diasConsecutivos = racha.diasConsecutivos,
                mejorRacha = racha.mejorRacha,
                ultimoDiaActivoEnDias = racha.ultimoDiaActivo,
            ),
        )
    }
}

private fun RachaEntity.aDominio() = Racha(
    diasConsecutivos = diasConsecutivos,
    mejorRacha = mejorRacha,
    ultimoDiaActivo = ultimoDiaActivoEnDias,
)
