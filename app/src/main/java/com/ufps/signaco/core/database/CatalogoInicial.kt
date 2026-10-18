package com.ufps.signaco.core.database

import com.ufps.signaco.core.database.entity.LeccionEntity
import com.ufps.signaco.core.database.entity.LeccionSenaCrossRef
import com.ufps.signaco.core.database.entity.SenaEntity
import com.ufps.signaco.core.reconocimiento.domain.VocabularioActivo
import com.ufps.signaco.core.senas.domain.model.Categoria

/**
 * Contenido con el que nace la base de datos: las 27 letras del abecedario dactilologico
 * y las lecciones que las agrupan. Lo inserta [PrecargaDelCatalogo] en el onCreate de Room.
 *
 * Las señas no llevan descripción de texto: la que había nunca se validó con material de LSC.
 * Cómo se hace cada seña lo muestra su imagen de referencia (ReferenciasEnRecursos).
 *
 * Las 27 letras estan en el catalogo, pero solo las 19 de
 * [VocabularioActivo] se practican. La disponibilidad NO se escribe a mano aqui: se deriva de
 * VocabularioActivo, que es la unica fuente de verdad, para que el catalogo y el filtrado del
 * clasificador no puedan separarse.
 *
 * Las excluidas siguen visibles en el diccionario y no pertenecen a ninguna leccion.
 */
object CatalogoInicial {

    const val LECCION_VOCALES = "leccion_vocales"
    const val LECCION_CONSONANTES_1 = "leccion_consonantes_1"
    const val LECCION_CONSONANTES_2 = "leccion_consonantes_2"
    const val LECCION_CONSONANTES_3 = "leccion_consonantes_3"
    const val LECCION_CONSONANTES_4 = "leccion_consonantes_4"

    val SENAS: List<SenaEntity> = listOf(
        vocal("A", 1),
        consonante("B", 2),
        consonante("C", 3),
        consonante("D", 4),
        vocal("E", 5),
        consonante("F", 6),
        consonante("G", 7),
        consonante("H", 8),
        vocal("I", 9),
        consonante("J", 10),
        consonante("K", 11),
        consonante("L", 12),
        consonante("M", 13),
        consonante("N", 14),
        SenaEntity(
            id = "NN",
            letra = "Ñ",
            nombre = "Letra Ñ",
            categoria = Categoria.CONSONANTES.name,
            orden = 15,
            disponible = VocabularioActivo.estaActiva("NN"),
        ),
        vocal("O", 16),
        consonante("P", 17),
        consonante("Q", 18),
        consonante("R", 19),
        consonante("S", 20),
        consonante("T", 21),
        vocal("U", 22),
        consonante("V", 23),
        consonante("W", 24),
        consonante("X", 25),
        consonante("Y", 26),
        consonante("Z", 27),
    )

    val LECCIONES: List<LeccionEntity> = listOf(
        LeccionEntity(
            id = LECCION_VOCALES,
            titulo = "Vocales",
            descripcion = "Las vocales del vocabulario. El mejor punto de partida: son las señas más estables.",
            categoria = Categoria.VOCALES.name,
            orden = 1,
        ),
        LeccionEntity(
            id = LECCION_CONSONANTES_1,
            titulo = "Consonantes B, D, F y G",
            descripcion = "Primer grupo de consonantes, con manos abiertas y curvas amplias.",
            categoria = Categoria.CONSONANTES.name,
            orden = 2,
        ),
        LeccionEntity(
            id = LECCION_CONSONANTES_2,
            titulo = "Consonantes J, L, P y Q",
            descripcion = "Segundo grupo, con las manos que apuntan hacia el lado.",
            categoria = Categoria.CONSONANTES.name,
            orden = 3,
        ),
        LeccionEntity(
            id = LECCION_CONSONANTES_3,
            titulo = "Consonantes R, S, T y V",
            descripcion = "Tercer grupo. La R pide cruzar los dedos con claridad.",
            categoria = Categoria.CONSONANTES.name,
            orden = 4,
        ),
        LeccionEntity(
            id = LECCION_CONSONANTES_4,
            titulo = "Consonantes W, X e Y",
            descripcion = "Último grupo de consonantes del vocabulario.",
            categoria = Categoria.CONSONANTES.name,
            orden = 5,
        ),
    )

    val RELACIONES: List<LeccionSenaCrossRef> =
        relacionar(LECCION_VOCALES, "A", "I", "O", "U") +
            relacionar(LECCION_CONSONANTES_1, "B", "D", "F", "G") +
            relacionar(LECCION_CONSONANTES_2, "J", "L", "P", "Q") +
            relacionar(LECCION_CONSONANTES_3, "R", "S", "T", "V") +
            relacionar(LECCION_CONSONANTES_4, "W", "X", "Y")

    private fun vocal(letra: String, orden: Int) =
        SenaEntity(
            id = letra,
            letra = letra,
            nombre = "Letra $letra",
            categoria = Categoria.VOCALES.name,
            orden = orden,
            disponible = VocabularioActivo.estaActiva(letra),
        )

    private fun consonante(letra: String, orden: Int) =
        SenaEntity(
            id = letra,
            letra = letra,
            nombre = "Letra $letra",
            categoria = Categoria.CONSONANTES.name,
            orden = orden,
            disponible = VocabularioActivo.estaActiva(letra),
        )

    private fun relacionar(leccionId: String, vararg senaIds: String) =
        senaIds.map { LeccionSenaCrossRef(leccionId = leccionId, senaId = it) }
}
