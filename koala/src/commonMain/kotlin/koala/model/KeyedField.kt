package koala.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.tapOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

data class KeyValue<K,T>(
    val key: K,
    val value: T,
)

class KeyedListTap<K, T>(
    private val source: MutableTap<List<T>>,
    private val newKey: () -> K
): MutableTap<List<KeyValue<K, T>>> {

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

fun <T> MutableTap<List<T>>.keyedListField(): MutableTap<List<KeyValue<Uuid, T>>> =
    KeyedListTap(this) { Uuid.random() }

fun <K, T> Tap<List<KeyValue<K, T>>>.keysField() = tapOf { items -> items.map { it.key } }

fun <K, T> MutableTap<List<KeyValue<K, T>>>.mutableTapOf(key: K) = mutableTapOf({ items -> items.first { it.key == key }.value }) { value ->
    map { if (it.key == key) it.copy(value = value) else it }
}