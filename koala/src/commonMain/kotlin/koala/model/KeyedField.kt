package koala.model

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

data class KeyValue<K,T>(
    val key: K,
    val value: T,
)

class KeyedListField<K, T>(
    private val source: MutableField<List<T>>,
    private val newKey: () -> K
): MutableField<List<KeyValue<K, T>>> {

    private var keys: List<K> = List(source.now.size) { newKey() }

    private fun keyed(list: List<T>): List<KeyValue<K, T>> {
        if (keys.size != list.size) {
            keys = List(list.size) { keys.getOrNull(it) ?: newKey() }
        }
        return list.mapIndexed { index, value -> KeyValue(keys[index], value) }
    }

    override val now get() = keyed(source.now)
    override val flow = source.flow.map(::keyed).distinctUntilChanged()

    private fun write(transform: (List<KeyValue<K, T>>) -> List<KeyValue<K, T>>) {
        source.update { list ->
            val next = transform(keyed(list))
            keys = next.map { it.key }
            next.map { it.value }
        }
    }

    override fun set(value: List<KeyValue<K, T>>) = write { value }

    override fun set(setter: List<KeyValue<K, T>>.() -> List<KeyValue<K, T>>) = write { it.setter() }

    override fun update(transform: (List<KeyValue<K, T>>) -> List<KeyValue<K, T>>) = write(transform)
}

fun <T> MutableField<List<T>>.keyedListField(): MutableField<List<KeyValue<Uuid, T>>> =
    KeyedListField(this) { Uuid.random() }

fun <K, T> Field<List<KeyValue<K, T>>>.keysField() = fieldOf { items -> items.map { it.key } }

fun <K, T> MutableField<List<KeyValue<K, T>>>.mutableFieldOf(key: K) = mutableFieldOf({ items -> items.first { it.key == key }.value }) { value ->
    map { if (it.key == key) it.copy(value = value) else it }
}