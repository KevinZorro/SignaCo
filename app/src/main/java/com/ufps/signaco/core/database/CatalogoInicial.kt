package com.ufps.signaco.core.database

import com.ufps.signaco.core.database.entity.LeccionEntity
import com.ufps.signaco.core.database.entity.LeccionSenaCrossRef
import com.ufps.signaco.core.database.entity.SenaEntity
import com.ufps.signaco.core.senas.domain.model.Categoria

/**
 * Contenido con el que nace la base de datos: las 27 letras del abecedario dactilologico
 * y las lecciones que las agrupan. Lo inserta [PrecargaDelCatalogo] en el onCreate de Room.
 *
 * Las descripciones son texto de trabajo: hay que validarlas con material de LSC antes de
 * la entrega. La estructura sirve tal cual; el texto se reemplaza sin tocar codigo.
 *
 * La enie ("NN") esta en el catalogo pero con disponible = false y no pertenece a ninguna
 * leccion: quedo fuera del vocabulario del MVP por variacion regional en el dataset LSC70.
 */
object CatalogoInicial {

    const val LECCION_VOCALES = "leccion_vocales"
    const val LECCION_CONSONANTES_1 = "leccion_consonantes_1"
    const val LECCION_CONSONANTES_2 = "leccion_consonantes_2"
    const val LECCION_CONSONANTES_3 = "leccion_consonantes_3"
    const val LECCION_CONSONANTES_4 = "leccion_consonantes_4"

    val SENAS: List<SenaEntity> = listOf(
        vocal("A", 1, "Puño cerrado con el pulgar extendido pegado al costado del índice."),
        consonante("B", 2, "Mano abierta, dedos juntos y estirados, pulgar doblado sobre la palma."),
        consonante("C", 3, "Dedos y pulgar curvados formando la letra C vista de lado."),
        consonante("D", 4, "Índice estirado hacia arriba; los demás dedos tocan la yema del pulgar."),
        vocal("E", 5, "Dedos doblados hacia la palma con el pulgar recogido debajo de ellos."),
        consonante("F", 6, "Pulgar e índice se tocan formando un círculo; los otros tres dedos estirados."),
        consonante("G", 7, "Índice y pulgar estirados y paralelos, apuntando hacia el lado."),
        consonante("H", 8, "Índice y medio juntos y estirados apuntando hacia el lado; el resto recogido."),
        vocal("I", 9, "Meñique estirado hacia arriba con el resto de la mano cerrada."),
        consonante("J", 10, "Meñique estirado que traza una J en el aire; la app evalúa la pose inicial."),
        consonante("K", 11, "Índice y medio en V con el pulgar apoyado entre los dos."),
        consonante("L", 12, "Índice hacia arriba y pulgar hacia el lado formando un ángulo recto."),
        consonante("M", 13, "Pulgar recogido bajo los tres primeros dedos doblados sobre la palma."),
        consonante("N", 14, "Pulgar recogido bajo el índice y el medio doblados sobre la palma."),
        SenaEntity(
            id = "NN",
            letra = "Ñ",
            nombre = "Letra Ñ",
            descripcion = "Como la N con un trazo ondulado encima. Fuera del vocabulario del MVP.",
            categoria = Categoria.CONSONANTES.name,
            orden = 15,
            disponible = false,
        ),
        vocal("O", 16, "Todos los dedos curvados tocando el pulgar, formando un círculo."),
        consonante("P", 17, "Como la K pero con la mano girada hacia abajo."),
        consonante("Q", 18, "Como la G pero con la mano girada hacia abajo."),
        consonante("R", 19, "Índice y medio estirados y cruzados entre sí."),
        consonante("S", 20, "Puño cerrado con el pulgar por delante de los otros dedos."),
        consonante("T", 21, "Pulgar asomando entre el índice y el medio, con la mano cerrada."),
        vocal("U", 22, "Índice y medio juntos y estirados hacia arriba."),
        consonante("V", 23, "Índice y medio estirados y separados en forma de V."),
        consonante("W", 24, "Índice, medio y anular estirados y separados."),
        consonante("X", 25, "Índice doblado en forma de gancho con el resto de la mano cerrada."),
        consonante("Y", 26, "Pulgar y meñique estirados, los demás dedos recogidos."),
        consonante("Z", 27, "Índice estirado que dibuja una Z en el aire; la app evalúa la pose inicial."),
    )

    val LECCIONES: List<LeccionEntity> = listOf(
        LeccionEntity(
            id = LECCION_VOCALES,
            titulo = "Vocales",
            descripcion = "Las cinco vocales. El mejor punto de partida: son las señas más estables.",
            categoria = Categoria.VOCALES.name,
            orden = 1,
        ),
        LeccionEntity(
            id = LECCION_CONSONANTES_1,
            titulo = "Consonantes B a G",
            descripcion = "Primer grupo de consonantes, con manos abiertas y curvas amplias.",
            categoria = Categoria.CONSONANTES.name,
            orden = 2,
        ),
        LeccionEntity(
            id = LECCION_CONSONANTES_2,
            titulo = "Consonantes H a M",
            descripcion = "Segundo grupo. La H exige separar bien los dedos para no confundirla.",
            categoria = Categoria.CONSONANTES.name,
            orden = 3,
        ),
        LeccionEntity(
            id = LECCION_CONSONANTES_3,
            titulo = "Consonantes N a S",
            descripcion = "Tercer grupo. La R pide cruzar los dedos con claridad.",
            categoria = Categoria.CONSONANTES.name,
            orden = 4,
        ),
        LeccionEntity(
            id = LECCION_CONSONANTES_4,
            titulo = "Consonantes T a Z",
            descripcion = "Último grupo de consonantes para cerrar el abecedario.",
            categoria = Categoria.CONSONANTES.name,
            orden = 5,
        ),
    )

    val RELACIONES: List<LeccionSenaCrossRef> =
        relacionar(LECCION_VOCALES, "A", "E", "I", "O", "U") +
            relacionar(LECCION_CONSONANTES_1, "B", "C", "D", "F", "G") +
            relacionar(LECCION_CONSONANTES_2, "H", "J", "K", "L", "M") +
            relacionar(LECCION_CONSONANTES_3, "N", "P", "Q", "R", "S") +
            relacionar(LECCION_CONSONANTES_4, "T", "V", "W", "X", "Y", "Z")

    private fun vocal(letra: String, orden: Int, descripcion: String) =
        SenaEntity(
            id = letra,
            letra = letra,
            nombre = "Letra $letra",
            descripcion = descripcion,
            categoria = Categoria.VOCALES.name,
            orden = orden,
            disponible = true,
        )

    private fun consonante(letra: String, orden: Int, descripcion: String) =
        SenaEntity(
            id = letra,
            letra = letra,
            nombre = "Letra $letra",
            descripcion = descripcion,
            categoria = Categoria.CONSONANTES.name,
            orden = orden,
            disponible = true,
        )

    private fun relacionar(leccionId: String, vararg senaIds: String) =
        senaIds.map { LeccionSenaCrossRef(leccionId = leccionId, senaId = it) }
}
