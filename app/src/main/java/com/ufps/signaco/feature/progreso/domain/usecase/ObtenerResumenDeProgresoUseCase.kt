package com.ufps.signaco.feature.progreso.domain.usecase

import com.ufps.signaco.core.comun.Reloj
import com.ufps.signaco.core.senas.domain.SenaRepository
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import com.ufps.signaco.feature.progreso.domain.RachaRepository
import com.ufps.signaco.feature.progreso.domain.model.AvanceDeSena
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import com.ufps.signaco.feature.progreso.domain.model.Racha
import com.ufps.signaco.feature.progreso.domain.model.ResumenProgreso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Arma el resumen que ve el usuario.
 *
 * Solo cuentan las señas disponibles en el MVP: la enie está en el catálogo pero no se
 * practica, así que no entra ni en el numerador ni en el denominador.
 */
class ObtenerResumenDeProgresoUseCase @Inject constructor(
    private val senaRepository: SenaRepository,
    private val progresoRepository: ProgresoRepository,
    private val rachaRepository: RachaRepository,
    private val reloj: Reloj,
) {
    operator fun invoke(): Flow<ResumenProgreso> {
        val lunes = ResumenProgreso.lunesDeLaSemana(reloj.hoyEnDias())
        return combine(
            senaRepository.observarSenas(),
            progresoRepository.observarProgreso(),
            progresoRepository.observarLeccionesCompletadas(),
            rachaRepository.observarRacha(),
            rachaRepository.observarDiasActivosDesde(lunes),
        ) { senas, progreso, completadas, racha, diasActivos ->
            armarResumen(senas, progreso, completadas, racha, diasActivos, lunes)
        }
    }

    private fun armarResumen(
        senas: List<Sena>,
        progreso: List<ProgresoSena>,
        completadas: Set<String>,
        racha: Racha,
        diasActivos: Set<Long>,
        lunes: Long,
    ): ResumenProgreso {
        val disponibles = senas.filter { it.disponible }
        val progresoPorSena = progreso.associateBy { it.senaId }
        val avance = disponibles
            .map { sena ->
                AvanceDeSena(
                    sena = sena,
                    progreso = progresoPorSena[sena.id] ?: ProgresoSena.vacio(sena.id),
                )
            }
            .filter { it.progreso.intentos > 0 }
            .sortedByDescending { it.progreso.aciertos }

        return ResumenProgreso(
            senasDominadas = avance.count { it.progreso.dominada },
            senasDisponibles = disponibles.size,
            leccionesCompletadas = completadas.size,
            rachaActual = racha.diasConsecutivos,
            mejorRacha = racha.mejorRacha,
            avancePorSena = avance,
            actividadDeLaSemana = List(ResumenProgreso.DIAS_DE_LA_SEMANA) { (lunes + it) in diasActivos },
        )
    }
}
