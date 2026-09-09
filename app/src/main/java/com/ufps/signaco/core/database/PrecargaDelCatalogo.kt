package com.ufps.signaco.core.database

import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.RoomDatabase
import com.ufps.signaco.core.comun.AlcanceDeAplicacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Siembra el catalogo la primera vez que se crea la base.
 *
 * onCreate corre dentro de la transaccion de creacion, asi que no se pueden usar los DAO
 * ahi mismo: la insercion se lanza en el alcance de la aplicacion, justo despues. Como todos
 * los DAO devuelven Flow, la UI que ya estuviera observando recibe el catalogo en cuanto se
 * escribe, sin necesidad de recargar nada.
 *
 * onOpen vuelve a sembrar si la tabla quedo vacia (por ejemplo si alguien limpio los datos
 * de la app sin borrar el archivo de la base).
 */
@Singleton
class PrecargaDelCatalogo @Inject constructor(
    private val proveedorDeBase: Provider<SignaCoDatabase>,
    @AlcanceDeAplicacion private val alcance: CoroutineScope,
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        sembrar()
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        sembrar()
    }

    private fun sembrar() {
        alcance.launch {
            try {
                val base = proveedorDeBase.get()
                if (base.senaDao().contarSenas() == 0) {
                    base.senaDao().insertarSenas(CatalogoInicial.SENAS)
                }
                if (base.leccionDao().contarLecciones() == 0) {
                    base.leccionDao().insertarLecciones(CatalogoInicial.LECCIONES)
                    base.leccionDao().insertarRelaciones(CatalogoInicial.RELACIONES)
                }
            } catch (e: Exception) {
                Log.e(ETIQUETA, "No se pudo precargar el catálogo", e)
            }
        }
    }

    private companion object {
        const val ETIQUETA = "PrecargaDelCatalogo"
    }
}
