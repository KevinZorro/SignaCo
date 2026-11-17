package com.ufps.signaco.core.preferencias.domain

import kotlinx.coroutines.flow.Flow

interface PreferenciasRepository {
    fun observarPreferencias(): Flow<PreferenciasUsuario>
    suspend fun cambiarMostrarPistas(activo: Boolean)
    suspend fun cambiarRetroalimentacionHaptica(activo: Boolean)
    suspend fun cambiarManoDominante(mano: ManoDominante)
    suspend fun cambiarVelocidadDeClips(velocidad: VelocidadDeClips)
    suspend fun cambiarSubtitulosEnClips(activo: Boolean)
    suspend fun cambiarRecordatoriosActivos(activo: Boolean)

    /** Tamaño, contraste y animaciones se guardan juntos: la pantalla los confirma con un botón. */
    suspend fun guardarApariencia(tamano: TamanoDeTexto, altoContraste: Boolean, reducirAnimaciones: Boolean)
    suspend fun marcarPrimerUsoCompletado()
}
