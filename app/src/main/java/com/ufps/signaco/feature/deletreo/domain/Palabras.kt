package com.ufps.signaco.feature.deletreo.domain

import java.text.Normalizer

/** Una palabra de la lista cerrada, ya partida en las letras del modelo ("calle" = C A L L E). */
data class PalabraDeLista(val palabra: String, val unidades: List<String>)

object Palabras {

    /**
     * "calle" -> C A L L E ; "año" -> A NN O ; se quitan tildes. LL y RR son dos letras base
     * repetidas: el modelo no tiene clase para los dígrafos, viven en la lista, no en el modelo.
     */
    fun aUnidades(palabra: String): List<String> = palabra.trim().uppercase().map { c ->
        if (c == 'Ñ') {
            "NN"
        } else {
            val base = Normalizer.normalize(c.toString(), Normalizer.Form.NFD)[0]
            require(base in 'A'..'Z') { "Carácter no soportado '$c' en \"$palabra\"" }
            base.toString()
        }
    }

    /**
     * Lee la lista (una palabra por línea, # comentarios). Conserva el orden del archivo, que es
     * la prioridad de desempate; descarta repetidas, líneas con espacios y palabras con letras
     * excluidas.
     */
    fun cargar(lineas: List<String>, excluidas: Set<String>): List<PalabraDeLista> {
        val vistas = HashSet<String>()
        val resultado = ArrayList<PalabraDeLista>()
        for (cruda in lineas) {
            val w = cruda.trim()
            if (w.isEmpty() || w.startsWith("#") || ' ' in w) continue
            val minuscula = w.lowercase()
            if (!vistas.add(minuscula)) continue
            val unidades = aUnidades(minuscula)
            if (unidades.any { it in excluidas }) continue
            resultado.add(PalabraDeLista(minuscula.uppercase(), unidades))
        }
        return resultado
    }
}
