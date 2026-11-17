package com.ufps.signaco.fakes

import com.ufps.signaco.core.comun.Reloj
import com.ufps.signaco.core.reconocimiento.domain.ResultadoClasificacion
import com.ufps.signaco.core.reconocimiento.domain.SignClassifier
import com.ufps.signaco.core.senas.domain.SenaRepository
import com.ufps.signaco.core.senas.domain.model.Categoria
import com.ufps.signaco.core.senas.domain.model.ReferenciaVisual
import com.ufps.signaco.core.senas.domain.model.Sena
import com.ufps.signaco.feature.lecciones.domain.LeccionRepository
import com.ufps.signaco.feature.lecciones.domain.model.Leccion
import com.ufps.signaco.feature.progreso.domain.ProgresoRepository
import com.ufps.signaco.feature.progreso.domain.RachaRepository
import com.ufps.signaco.feature.progreso.domain.model.ProgresoSena
import com.ufps.signaco.feature.progreso.domain.model.Racha
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Dobles de prueba escritos a mano. No usamos una librería de mocks a propósito: son pocas
 * interfaces, y así las pruebas se leen sin conocer una API extra.
 */

class RelojFijo(var dia: Long) : Reloj {
    override fun hoyEnDias(): Long = dia
}

class FakeSenaRepository(senas: List<Sena> = emptyList()) : SenaRepository {
    val senas = MutableStateFlow(senas)
    override fun observarSenas(): Flow<List<Sena>> = senas
    override fun observarSena(senaId: String): Flow<Sena?> =
        senas.map { lista -> lista.firstOrNull { it.id == senaId } }
}

class FakeLeccionRepository(lecciones: List<Leccion> = emptyList()) : LeccionRepository {
    val lecciones = MutableStateFlow(lecciones)
    override fun observarLecciones(): Flow<List<Leccion>> = lecciones
    override fun observarLeccion(leccionId: String): Flow<Leccion?> =
        lecciones.map { lista -> lista.firstOrNull { it.id == leccionId } }
}

class FakeProgresoRepository(
    progresoInicial: List<ProgresoSena> = emptyList(),
    completadasIniciales: Set<String> = emptySet(),
) : ProgresoRepository {

    val progreso = MutableStateFlow(progresoInicial)
    val completadas = MutableStateFlow(completadasIniciales)

    /** Historial de llamadas, para verificar qué se registró y en qué orden. */
    val intentosRegistrados = mutableListOf<Pair<String, Boolean>>()

    override fun observarProgreso(): Flow<List<ProgresoSena>> = progreso

    override fun observarProgresoDeSena(senaId: String): Flow<ProgresoSena?> =
        progreso.map { lista -> lista.firstOrNull { it.senaId == senaId } }

    override suspend fun registrarIntento(senaId: String, acerto: Boolean) {
        intentosRegistrados += senaId to acerto
        progreso.value = progreso.value.toMutableList().also { lista ->
            val indice = lista.indexOfFirst { it.senaId == senaId }
            val anterior = lista.getOrNull(indice) ?: ProgresoSena.vacio(senaId)
            val nuevo = anterior.copy(
                intentos = anterior.intentos + 1,
                aciertos = anterior.aciertos + if (acerto) 1 else 0,
            )
            if (indice >= 0) lista[indice] = nuevo else lista += nuevo
        }
    }

    override fun observarLeccionesCompletadas(): Flow<Set<String>> = completadas

    override suspend fun marcarLeccionCompletada(
        leccionId: String,
        aciertosQuiz: Int,
        totalQuiz: Int,
    ) {
        completadas.value = completadas.value + leccionId
    }

    override suspend fun borrarTodo() {
        progreso.value = emptyList()
        completadas.value = emptySet()
    }
}

class FakeRachaRepository(inicial: Racha = Racha.INICIAL) : RachaRepository {
    val racha = MutableStateFlow(inicial)
    override fun observarRacha(): Flow<Racha> = racha
    override suspend fun obtenerRacha(): Racha = racha.value
    override suspend fun guardarRacha(racha: Racha) {
        this.racha.value = racha
    }

    val diasActivos = MutableStateFlow<Set<Long>>(emptySet())

    override suspend fun registrarDiaActivo(dia: Long) {
        diasActivos.value = diasActivos.value + dia
    }

    override fun observarDiasActivosDesde(dia: Long): Flow<Set<Long>> =
        diasActivos.map { dias -> dias.filter { it >= dia }.toSet() }
}

/** Clasificador programable: devuelve lo que la prueba le diga, sin retardo. */
class FakeClasificadorProgramable(
    var siguienteResultado: ResultadoClasificacion = ResultadoClasificacion.ManoNoDetectada,
) : SignClassifier {
    var ultimosLandmarks: FloatArray? = null
        private set

    override suspend fun clasificar(landmarks: FloatArray): ResultadoClasificacion {
        ultimosLandmarks = landmarks
        return siguienteResultado
    }
}

fun senaDePrueba(
    id: String,
    orden: Int = 1,
    categoria: Categoria = Categoria.VOCALES,
    disponible: Boolean = true,
    referencia: ReferenciaVisual? = null,
) = Sena(
    id = id,
    letra = id,
    nombre = "Letra $id",
    categoria = categoria,
    orden = orden,
    disponible = disponible,
    referencia = referencia,
)

/** Una imagen de referencia inventada: en las pruebas basta con que no sea null. */
fun imagenDePrueba(id: String) = ReferenciaVisual.Imagen(id.hashCode())

fun leccionDePrueba(
    id: String,
    senas: List<Sena>,
    categoria: Categoria = Categoria.VOCALES,
    orden: Int = 1,
) = Leccion(
    id = id,
    titulo = "Lección $id",
    descripcion = "Descripción de $id",
    categoria = categoria,
    orden = orden,
    senas = senas,
)

/** Repositorio que siempre falla, para probar el camino de error de los ViewModel. */
class LeccionRepositoryQueFalla : LeccionRepository {
    override fun observarLecciones(): Flow<List<Leccion>> =
        flow { throw IllegalStateException("falla simulada") }

    override fun observarLeccion(leccionId: String): Flow<Leccion?> =
        flow { throw IllegalStateException("falla simulada") }
}

class SenaRepositoryQueFalla : SenaRepository {
    override fun observarSenas(): Flow<List<Sena>> =
        flow { throw IllegalStateException("falla simulada") }

    override fun observarSena(senaId: String): Flow<Sena?> =
        flow { throw IllegalStateException("falla simulada") }
}

class FakePreferenciasRepository(
    inicial: com.ufps.signaco.core.preferencias.domain.PreferenciasUsuario = com.ufps.signaco.core.preferencias.domain.PreferenciasUsuario(),
) : com.ufps.signaco.core.preferencias.domain.PreferenciasRepository {
    val preferencias = MutableStateFlow(inicial)

    override fun observarPreferencias(): Flow<com.ufps.signaco.core.preferencias.domain.PreferenciasUsuario> = preferencias

    override suspend fun cambiarMostrarPistas(activo: Boolean) {
        preferencias.value = preferencias.value.copy(mostrarPistas = activo)
    }

    override suspend fun cambiarRetroalimentacionHaptica(activo: Boolean) {
        preferencias.value = preferencias.value.copy(retroalimentacionHaptica = activo)
    }

    override suspend fun cambiarManoDominante(mano: com.ufps.signaco.core.preferencias.domain.ManoDominante) {
        preferencias.value = preferencias.value.copy(manoDominante = mano)
    }

    override suspend fun cambiarVelocidadDeClips(velocidad: com.ufps.signaco.core.preferencias.domain.VelocidadDeClips) {
        preferencias.value = preferencias.value.copy(velocidadDeClips = velocidad)
    }

    override suspend fun cambiarSubtitulosEnClips(activo: Boolean) {
        preferencias.value = preferencias.value.copy(subtitulosEnClips = activo)
    }

    override suspend fun cambiarRecordatoriosActivos(activo: Boolean) {
        preferencias.value = preferencias.value.copy(recordatoriosActivos = activo)
    }

    override suspend fun guardarApariencia(
        tamano: com.ufps.signaco.core.preferencias.domain.TamanoDeTexto,
        altoContraste: Boolean,
        reducirAnimaciones: Boolean,
    ) {
        preferencias.value = preferencias.value.copy(
            tamanoDeTexto = tamano,
            altoContraste = altoContraste,
            reducirAnimaciones = reducirAnimaciones,
        )
    }

    override suspend fun marcarPrimerUsoCompletado() {
        preferencias.value = preferencias.value.copy(primerUsoCompletado = true)
    }
}

class FakeProgramadorDeRecordatorios : com.ufps.signaco.core.notificaciones.domain.ProgramadorDeRecordatorios {
    var programado = false
    var ultimaHora = 0
    var ultimoMinuto = 0

    override fun programarRecordatorioDiario(hora: Int, minuto: Int) {
        programado = true
        ultimaHora = hora
        ultimoMinuto = minuto
    }

    override fun cancelarRecordatorio() {
        programado = false
    }
}
