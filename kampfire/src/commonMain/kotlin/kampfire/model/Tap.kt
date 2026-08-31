package kampfire.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface Tap<Value> {
    val flow: Flow<Value>
    val now: Value
}

interface MutableTap<Value>: Tap<Value> {
    fun update(transform: (Value) -> Value)
    fun set(value: Value)
    fun set(setter: Value.() -> Value)
}

fun <State, Value> MutableTap<State>.mutableTapOf(
    readValue: (State) -> Value,
    applyFlow: (Flow<State>) -> Flow<Value> = { it.map(readValue).distinctUntilChanged() },
    writeValue: State.(Value) -> State
): MutableTap<Value> = MutableLens(this, applyFlow, readValue, writeValue)

fun <State, Value> Tap<State>.tapOf(
    applyFlow: ((Flow<State>) -> Flow<Value>)? = null,
    readValue: (State) -> Value,
): Tap<Value> = Lens(this, applyFlow ?: { it.map(readValue).distinctUntilChanged() }, readValue)

fun <T> Tap<T>.refine(applyFlow: (Flow<T>) -> Flow<T>): Tap<T> = tapOf(applyFlow) { it }

inline fun <Base, reified T : Base> MutableTap<Base>.narrow(): MutableTap<T> =
    mutableTapOf(readValue = { it as T }, writeValue = { it })

fun <T> Tap<T>.reactIn(scope: CoroutineScope, block: suspend (T) -> Unit): Tap<T> {
    scope.launch {
        flow.collect { value ->
            block(value)
        }
    }
    return this
}

class MutableLens<State, Value>(
    private val store: MutableTap<State>,
    applyFlow: (Flow<State>) -> Flow<Value>,
    val readValue: (State) -> Value,
    val writeValue: State.(Value) -> State
): MutableTap<Value> {
    override val now: Value get() = readValue(store.now)
    override val flow = applyFlow(store.flow)

    override fun update(transform: (Value) -> Value) {
        store.update { it.writeValue(transform(readValue(it))) }
    }
    override fun set(value: Value) {
        store.update { it.writeValue(value) }
    }
    override fun set(setter: Value.() -> Value) {
        store.update { it.writeValue(readValue(it).setter()) }
    }
}

class Lens<State, Value>(
    private val store: Tap<State>,
    applyFlow: (Flow<State>) -> Flow<Value>,
    val readValue: (State) -> Value,
): Tap<Value> {
    override val now: Value get() = readValue(store.now)
    override val flow = applyFlow(store.flow)
}

class CombinedTap<A, B, Value>(
    private val tapA: Tap<A>,
    private val tapB: Tap<B>,
    applyFlow: (Flow<Value>) -> Flow<Value>,
    val readValue: (A, B) -> Value,
): Tap<Value> {
    override val now: Value get() = readValue(tapA.now, tapB.now)
    override val flow = applyFlow(combine(tapA.flow, tapB.flow, readValue))
}

fun <A, B, Value> Tap<A>.combine(
    b: Tap<B>,
    applyFlow: ((Flow<Value>) -> Flow<Value>)? = null,
    readValue: (A, B) -> Value,
): Tap<Value> = CombinedTap(this, b, applyFlow ?: { it.distinctUntilChanged() }, readValue)