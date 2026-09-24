package koala.dom

import org.koin.core.Koin

/** The app's service container, over its Koin instance. A view reaches a service with [get]. */
class AppContainer(val koin: Koin) {
    inline fun <reified T> get(): T = koin.get()
    inline fun <reified T> getOrNull(): T? = koin.getOrNull()
}

/** Anything holding the [AppContainer], such as a view. */
interface AppFacade {
    val app: AppContainer
}