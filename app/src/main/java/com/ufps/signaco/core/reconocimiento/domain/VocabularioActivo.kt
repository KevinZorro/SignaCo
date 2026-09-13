package com.ufps.signaco.core.reconocimiento.domain

/**
 * Las letras que la app enseña de verdad.
 *
 * El modelo clasifica 28 clases, pero solo 19 letras entran al vocabulario. Las excluidas se
 * filtran aquí, en la app: el modelo no se reentrena ni se modifica.
 *
 * Motivos de cada exclusión:
 * - Z y NN (enie): son señas dinámicas y el clasificador es de poses estáticas.
 * - C, E, H, K, M, N: se confunden de forma sistemática con cámara real, por la familia de
 *   puño cerrado y por articulación fina de dedos.
 *
 * El catálogo de Room marca estas mismas letras con disponible = false, así que no aparecen
 * en lecciones ni en quiz; el diccionario sí las muestra.
 */
object VocabularioActivo {

    val LETRAS: List<String> = listOf(
        "A", "B", "D", "F", "G", "I", "J", "L", "O",
        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
    )

    val EXCLUIDAS: List<String> = Alfabeto.LETRAS.filterNot { it in LETRAS }

    fun estaActiva(letra: String): Boolean = letra in LETRAS

    /**
     * Índices de la salida del modelo que corresponden a letras activas, según el orden de
     * etiquetas del config. Se calcula una vez y se reutiliza en cada frame.
     */
    fun indicesActivosEn(etiquetas: List<String>): IntArray =
        etiquetas.indices.filter { etiquetas[it] in LETRAS }.toIntArray()
}
