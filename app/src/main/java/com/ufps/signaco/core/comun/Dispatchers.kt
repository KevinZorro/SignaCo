package com.ufps.signaco.core.comun

import javax.inject.Qualifier

/**
 * Calificadores para inyectar dispatchers en vez de usar [kotlinx.coroutines.Dispatchers]
 * directamente. Asi las pruebas pueden sustituirlos por un dispatcher de prueba.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherIo

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherDefault

/** Alcance de corrutinas que vive tanto como la aplicacion (precarga de la base, etc.). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AlcanceDeAplicacion
