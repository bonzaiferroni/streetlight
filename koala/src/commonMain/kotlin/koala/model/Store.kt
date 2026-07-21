package koala.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

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

fun <State, Value> MutableField<State>.mutableFieldOf(
    readValue: (State) -> Value,
    applyFlow: (Flow<State>) -> Flow<Value> = { it.map(readValue).distinctUntilChanged() },
    onValue: State.(Value) -> State
): MutableField<Value> = MutableStoreField(this, applyFlow, readValue, onValue)

fun <State, Value> Field<State>.fieldOf(
    applyFlow: ((Flow<State>) -> Flow<Value>)? = null,
    readValue: (State) -> Value,
): Field<Value> = StoreField(this, applyFlow ?: { it.map(readValue).distinctUntilChanged() }, readValue)

class MutableStoreField<State, Value>(
    private val store: MutableField<State>,
    applyFlow: (Flow<State>) -> Flow<Value>,
    val readValue: (State) -> Value,
    val onValue: State.(Value) -> State
): MutableField<Value> {
    override val now: Value get() = readValue(store.now)
    override val flow = applyFlow(store.flow)

    override fun update(transform: (Value) -> Value) = store.update { it.onValue(transform(readValue(it))) }
    override fun set(value: Value) {
        store.update { it.onValue(value) }
    }
}

class StoreField<State, Value>(
    private val store: Field<State>,
    applyFlow: (Flow<State>) -> Flow<Value>,
    val readValue: (State) -> Value,
): Field<Value> {
    override val now: Value get() = readValue(store.now)
    override val flow = applyFlow(store.flow)
}

interface Field<Value> {
    val flow: Flow<Value>
    val now: Value
}

interface MutableField<Value>: Field<Value> {
    fun update(transform: (Value) -> Value)
    fun set(value: Value)
}

fun <T, State> Store<State>.protoFieldOf(
    readValue: (State) -> T,
    onValue: State.(T) -> State
) = StateFieldProto(
    flow.dedup { readValue(it) }
) { value -> setValue { onValue(it, value) } }

data class StateFieldProto<T>(
    val flow: Flow<T>,
    val onValue: (T) -> Unit,
)