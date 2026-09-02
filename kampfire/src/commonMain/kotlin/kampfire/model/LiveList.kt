package kampfire.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription

class LiveList<T, K>(initialItems: List<T>, private val keyOf: (T) -> K) {
    private val state = storeOf(LiveListState(initialItems.size))

    val liveItems: List<T> field = initialItems.toMutableList()
    private val changes = MutableSharedFlow<ListChange<T>>()
    val changeFlow = changes.onSubscription { emit(ListChange.Insert(0, liveItems)) }

    suspend fun insertAt(index: Int, items: List<T>) {
        liveItems.addAll(index, items)
        state.set { copy(size = liveItems.size) }
        changes.emit(ListChange.Insert(index, items))
    }

    suspend fun insertBefore(key: K, item: T) {
        val index = liveItems.indexOfFirst { keyOf(it) == key }
        if (index >= 0) insertAt(index, item)
    }

    suspend fun insertAfter(key: K, item: T) {
        val index = liveItems.indexOfFirst { keyOf(it) == key }
        if (index >= 0) insertAt(index + 1, item)
    }

    suspend fun insertBeforeFirst(item: T, predicate: (T) -> Boolean) {
        val index = liveItems.indexOfFirst(predicate)
        if (index >= 0) insertAt(index, item)
    }

    suspend fun insertAfterFirst(item: T, predicate: (T) -> Boolean) {
        val index = liveItems.indexOfFirst(predicate)
        if (index >= 0) insertAt(index + 1, item)
    }

    suspend fun replaceAt(index: Int, item: T) {
        liveItems[index] = item
        changes.emit(ListChange.Replace(index, item))
    }

    suspend fun replace(item: T) {
        val key = keyOf(item)
        val index = liveItems.indexOfFirst { keyOf(it) == key }
        if (index >= 0) replaceAt(index, item)
    }

    suspend fun replaceFirst(item: T, predicate: (T) -> Boolean) {
        val index = liveItems.indexOfFirst(predicate)
        if (index >= 0) replaceAt(index, item)
    }

    suspend fun removeAt(index: Int, count: Int = 1) {
        liveItems.subList(index, index + count).clear()
        state.set { copy(size = liveItems.size) }
        changes.emit(ListChange.Remove(index, count))
    }

    suspend fun remove(key: K) {
        val index = liveItems.indexOfFirst { keyOf(it) == key }
        if (index >= 0) removeAt(index)
    }

    suspend fun removeFirst(predicate: (T) -> Boolean) {
        val index = liveItems.indexOfFirst(predicate)
        if (index >= 0) removeAt(index)
    }

    suspend fun clear() {
        liveItems.clear()
        state.set { copy(size = 0) }
        changes.emit(ListChange.Clear)
    }

    suspend fun add(item: T) = insertAt(liveItems.size, listOf(item))
    suspend fun add(items: List<T>) = insertAt(liveItems.size, items)
    suspend fun insertAt(index: Int, item: T) = insertAt(index, listOf(item))
}

data class LiveListState(
    val size: Int,
)

sealed interface ListChange<out T> {
    data class Insert<T>(val index: Int, val items: List<T>) : ListChange<T>
    data class Replace<T>(val index: Int, val item: T) : ListChange<T>
    data class Remove(val index: Int, val count: Int) : ListChange<Nothing>
    data object Clear : ListChange<Nothing>
}