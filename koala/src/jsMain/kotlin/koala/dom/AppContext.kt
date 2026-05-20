package koala.dom

import kotlinx.coroutines.CoroutineScope
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

class AppContext(val koin: Koin) {
    inline fun <reified T> get(): T = koin.get()

    inline fun <reified T> getCoroutineScoped(scope: CoroutineScope): T = koin.get { parametersOf(scope) }
    inline fun <reified T> getCoroutineScoped(param: Any?, scope: CoroutineScope): T = koin.get { parametersOf(param, scope)}
    inline fun <reified T> getCoroutineScoped(param1: Any?, param2: Any?, scope: CoroutineScope): T =
        koin.get { parametersOf(param1, param2, scope)}
}