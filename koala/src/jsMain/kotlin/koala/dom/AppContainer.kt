package koala.dom

import org.koin.core.Koin

class AppContainer(val koin: Koin) {
    inline fun <reified T> get(): T = koin.get()
    inline fun <reified T> getOrNull(): T? = koin.getOrNull()
}

interface AppFacade {
    val app: AppContainer
}