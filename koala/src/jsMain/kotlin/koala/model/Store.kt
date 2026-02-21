package koala.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Store<T>(
    initialValue: T,
) {
    private val state = MutableStateFlow(initialValue)

    val flow = state.asStateFlow()
    val now get() = state.value

    fun set(setter: (T) -> T) {
        state.value = setter(now)
    }
}

fun <T> storeOf(initialValue: T) = Store(initialValue)