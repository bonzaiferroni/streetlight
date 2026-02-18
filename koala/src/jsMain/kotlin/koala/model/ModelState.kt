package koala.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModelState<T>(
    initialValue: T,
) {
    private val state = MutableStateFlow(initialValue)

    val flow = state.asStateFlow()
    val now get() = state.value

    fun set(setter: (T) -> T) {
        state.value = setter(now)
    }
}

fun <T> stateOf(initialValue: T) = ModelState(initialValue)