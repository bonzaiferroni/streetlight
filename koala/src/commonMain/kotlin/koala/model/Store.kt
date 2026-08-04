package koala.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class Store<T>(
    initialValue: T,
): MutableField<T> {
    private val state = MutableStateFlow(initialValue)

    override val flow = state.asStateFlow()
    override val now get() = state.value

    override fun set(value: T) {
        state.update { value }
    }

    fun set(setter: T.() -> T) {
        state.update { it.setter() }
    }

    @Deprecated("use set")
    fun setValue(setter: (T) -> T) {
        state.value = setter(now)
    }

    @Deprecated("use set")
    fun setValue(value: T) {
        state.value = value
    }

    override fun update(transform: (T) -> T) {
        state.update(transform)
    }
}

fun <T> storeOf(initialValue: T) = Store(initialValue)

fun <T> Store<List<T>>.addAll(values: List<T>) {
    set(now + values)
}