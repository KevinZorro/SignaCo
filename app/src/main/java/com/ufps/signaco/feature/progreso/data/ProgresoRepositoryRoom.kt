package com.ufps.signaco.feature.progreso.data

import com.ufps.signaco.core.database.dao.LeccionDao
import com.ufps.signaco.core.database.dao.ProgresoDao
import com.ufps.signaco.core.database.entity.LeccionCompletadaEntity
import com.ufps.signaco.core.database.entity.ProgresoSenaEntity
import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProgresoRepositoryRoom @Inject constructor(
    private val progresoDao: ProgresoDao,
    private val leccionDao: LeccionDao,
) : ProgresoRepository {

    override fun observarProgreso(): Flow<List<ProgresoSena>> =
        progresoDao.observarProgreso().map { filas -> filas.map { it.aDominio() } }

    override fun observarProgresoDeSena(senaId: String): Flow<ProgresoSena?> =
        progresoDao.observarProgresoDeSena(senaId).map { it?.aDominio() }

    override suspend fun registrarIntento(senaId: String, acerto: Boolean) {
        progresoDao.registrarIntento(senaId, acerto, System.currentTimeMillis())
    }

    override fun observarLeccionesCompletadas(): Flow<Set<String>> =
        leccionDao.observarLeccionesCompletadas()
            .map { filas -> filas.map { it.leccionId }.toSet() }

    override suspend fun marcarLeccionCompletada(
        leccionId: String,
        aciertosQuiz: Int,
        totalQuiz: Int,
    ) {
        leccionDao.guardarLeccionCompletada(
            LeccionCompletadaEntity(
                leccionId = leccionId,
                completadaEnMillis = System.currentTimeMillis(),
                aciertosQuiz = aciertosQuiz,
                totalQuiz = totalQuiz,
            ),
        )
    }

    override suspend fun borrarTodo() {
        progresoDao.borrarTodo()
    }
}

private fun ProgresoSenaEntity.aDominio() = ProgresoSena(
    senaId = senaId,
    intentos = intentos,
    aciertos = aciertos,
)
