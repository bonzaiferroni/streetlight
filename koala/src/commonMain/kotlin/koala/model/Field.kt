package koala.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface Field<Value> {
    val flow: Flow<Value>
    val now: Value
}

interface MutableField<Value>: Field<Value> {
    fun update(transform: (Value) -> Value)
    fun set(value: Value)
}

fun <State, Value> MutableField<State>.mutableFieldOf(
    readValue: (State) -> Value,
    applyFlow: (Flow<State>) -> Flow<Value> = { it.map(readValue).distinctUntilChanged() },
    onValue: State.(Value) -> State
): MutableField<Value> = MutableStoreField(this, applyFlow, readValue, onValue)

fun <State, Value> Field<State>.fieldOf(
    applyFlow: ((Flow<State>) -> Flow<Value>)? = null,
    readValue: (State) -> Value,
): Field<Value> = StoreField(this, applyFlow ?: { it.map(readValue).distinctUntilChanged() }, readValue)

fun <T> Field<T>.refine(applyFlow: (Flow<T>) -> Flow<T>): Field<T> = fieldOf(applyFlow) { it }

fun <T> Field<T>.reactIn(scope: CoroutineScope, block: (T) -> Unit): Field<T> {
    scope.launch {
        flow.collect {
            block(it)
        }
    }
    return this
}

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

class CombinedField<A, B, Value>(
    private val fieldA: Field<A>,
    private val fieldB: Field<B>,
    applyFlow: (Flow<Value>) -> Flow<Value>,
    val readValue: (A, B) -> Value,
): Field<Value> {
    override val now: Value get() = readValue(fieldA.now, fieldB.now)
    override val flow = applyFlow(combine(fieldA.flow, fieldB.flow, readValue))
}

fun <A, B, Value> Field<A>.combine(
    b: Field<B>,
    applyFlow: ((Flow<Value>) -> Flow<Value>)? = null,
    readValue: (A, B) -> Value,
): Field<Value> = CombinedField(this, b, applyFlow ?: { it.distinctUntilChanged() }, readValue)