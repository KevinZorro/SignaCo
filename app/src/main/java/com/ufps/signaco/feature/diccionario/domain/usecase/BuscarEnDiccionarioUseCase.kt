package com.ufps.signaco.feature.diccionario.domain.usecase

import com.ufps.signaco.core.senas.domain.SenaRepository
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.Sena
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.util.Locale

/**
 * Catálogo completo del diccionario, filtrado por lo que escriba el usuario.
 *
 * Incluye las señas que no están en el vocabulario del MVP: en el diccionario se pueden
 * consultar, aunque no se practiquen. La pantalla las marca como no disponibles.
 */
class BuscarEnDiccionarioUseCase @Inject constructor(
    private val senaRepository: SenaRepository,
) {
    /** @param categoria null muestra todas las categorías. */
    operator fun invoke(consulta: String, categoria: Categoria? = null): Flow<List<Sena>> =
        senaRepository.observarSenas().map { senas ->
            val texto = consulta.trim().lowercase(Locale.ROOT)
            senas
                .filter { categoria == null || it.categoria == categoria }
                .filter { texto.isEmpty() || coincide(it, texto) }
        }

    private fun coincide(sena: Sena, texto: String): Boolean =
        sena.letra.lowercase(Locale.ROOT).startsWith(texto) ||
            sena.nombre.lowercase(Locale.ROOT).contains(texto) ||
            sena.descripcion.lowercase(Locale.ROOT).contains(texto)
}
