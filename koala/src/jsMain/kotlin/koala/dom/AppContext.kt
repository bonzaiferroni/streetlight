package koala.dom

import kotlinx.coroutines.CoroutineScope
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

class AppContext(val koin: Koin) {
    inline fun <reified T> get(): T = koin.get()
}