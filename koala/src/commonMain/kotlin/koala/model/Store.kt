package koala.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

open class Store<T>(
    initialValue: T,
): MutableTap<T> {
    private val state = MutableStateFlow(initialValue)

    override val flow = state.asStateFlow()
    override val now get() = state.value

    override fun set(value: T) {
        state.update { value }
    }

    override fun set(setter: T.() -> T) {
        state.update { it.setter() }
    }

    override fun update(transform: (T) -> T) {
        state.update(transform)
    }
}

fun <T> storeOf(initialValue: T) = Store(initialValue)

fun <T> Store<List<T>>.addAll(values: List<T>) {
    set(now + values)
}