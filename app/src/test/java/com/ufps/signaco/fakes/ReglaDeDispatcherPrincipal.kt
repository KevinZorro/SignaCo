package com.ufps.signaco.fakes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Los ViewModel usan viewModelScope, que corre en Dispatchers.Main. En una prueba de JVM no
 * hay Main, así que se sustituye por un dispatcher de prueba mientras dura cada prueba.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReglaDeDispatcherPrincipal(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
