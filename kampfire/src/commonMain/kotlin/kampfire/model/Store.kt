package kampfire.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A [MutableTap] holding its own state in a [StateFlow].
 *
 * A view model holds one store of its state and exposes lenses onto it with [tapOf] and [mutableTapOf].
 */
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

/** Creates a [Store] holding [initialValue]. */
fun <T> storeOf(initialValue: T) = Store(initialValue)

/** Appends [values] to the list. */
fun <T> Store<List<T>>.addAll(values: List<T>) {
    set(now + values)
}