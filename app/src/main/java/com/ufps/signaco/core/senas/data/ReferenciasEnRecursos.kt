package com.ufps.signaco.core.senas.data

import com.ufps.signaco.R
import com.ufps.signaco.core.senas.domain.ReferenciasVisuales
import com.ufps.signaco.core.senas.domain.model.ReferenciaVisual
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Imágenes de referencia empaquetadas en res/drawable-nodpi, asociadas por identificador de
 * seña. La fuente de cada imagen está en docs/fuentes_senas.md (RD06).
 *
 * El mapa se escribe a mano y no se arma buscando el recurso por nombre: así el compilador
 * avisa si falta un archivo y el reductor de recursos no puede borrar una imagen en uso.
 *
 * Para agregar una imagen: copiar sena_<letra>.webp a res/drawable-nodpi, añadir su línea
 * aquí y su fila en docs/fuentes_senas.md.
 */
@Singleton
class ReferenciasEnRecursos @Inject constructor() : ReferenciasVisuales {

    override fun de(senaId: String): ReferenciaVisual? =
        IMAGENES[senaId]?.let { ReferenciaVisual.Imagen(it) }

    companion object {
        /** Identificador de seña a recurso. Las letras que no están aquí no tienen imagen. */
        val IMAGENES: Map<String, Int> = mapOf(
            "A" to R.drawable.sena_a,
            "B" to R.drawable.sena_b,
            "D" to R.drawable.sena_d,
            "F" to R.drawable.sena_f,
            "G" to R.drawable.sena_g,
        )
    }
}
