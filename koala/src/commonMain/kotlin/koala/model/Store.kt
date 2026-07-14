package koala.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

open class Store<T>(
    initialValue: T,
) {
    private val state = MutableStateFlow(initialValue)

    val flow = state.asStateFlow()
    val now get() = state.value

    fun set(value: T) {
        state.value = value
    }

    fun set(setter: (T) -> T) {
        state.value = setter(now)
    }

    fun setValue(value: T) {
        state.value = value
    }
}

fun <T> storeOf(initialValue: T) = Store(initialValue)

fun <T, State> Store<State>.fieldOf(
    readValue: (State) -> T,
    onValue: State.(T) -> State
) = StateField(
    flow.tap { readValue(it) }
) { value -> set { onValue(it, value) } }

data class StateField<T>(
    val flow: Flow<T>,
    val onValue: (T) -> Unit,
)