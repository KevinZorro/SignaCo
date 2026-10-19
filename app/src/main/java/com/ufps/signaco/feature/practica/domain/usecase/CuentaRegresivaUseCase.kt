package com.ufps.signaco.feature.practica.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * La cuenta regresiva antes de cada intento: emite 3, 2 y 1, un segundo cada uno, y luego 0,
 * que es el momento en que el intento empieza solo.
 *
 * Existe para que la persona no tenga que acordarse de tocar «Iniciar intento»: tiene tres
 * segundos para poner la mano frente a la cámara. La usan la práctica y el quiz con cámara.
 *
 * Cancelar la cuenta es cancelar quien la recolecta; aquí no hay estado.
 */
class CuentaRegresivaUseCase @Inject constructor() {

    operator fun invoke(segundos: Int = SEGUNDOS): Flow<Int> = flow {
        for (restantes in segundos downTo 1) {
            emit(restantes)
            delay(UN_SEGUNDO_MS)
        }
        emit(INICIO)
    }

    companion object {
        const val SEGUNDOS = 3

        /** El valor que marca que la cuenta terminó y el intento empieza. */
        const val INICIO = 0

        private const val UN_SEGUNDO_MS = 1_000L
    }
}
