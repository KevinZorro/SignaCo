package com.ufps.signaco.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking

/**
 * Una base en memoria con el catálogo ya sembrado, para las pruebas que arman ViewModels y
 * repositorios reales a mano (sin Hilt de pruebas).
 */
fun baseConCatalogo(): SignaCoDatabase {
    val contexto = ApplicationProvider.getApplicationContext<Context>()
    val base = Room.inMemoryDatabaseBuilder(contexto, SignaCoDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    runBlocking {
        base.senaDao().insertarSenas(CatalogoInicial.SENAS)
        base.leccionDao().insertarLecciones(CatalogoInicial.LECCIONES)
        base.leccionDao().insertarRelaciones(CatalogoInicial.RELACIONES)
    }
    return base
}
