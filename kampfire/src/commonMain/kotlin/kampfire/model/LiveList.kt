package kampfire.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription

/**
 * A list that emits each change as a [ListChange], so a view can update in place rather than rebuild.
 *
 * Items are identified by [keyOf]. Inserting an item whose key is already present moves it.
 */
class LiveList<T, K>(initialItems: List<T>, private val keyOf: (T) -> K) {
    private val state = storeOf(LiveListState(initialItems.size))

    val liveItems: List<T> field = initialItems.toMutableList()
    private val changes = MutableSharedFlow<ListChange<T>>()
    val changeFlow = changes.onSubscription { emit(ListChange.Insert(0, liveItems)) }

    /** Inserts [items] at [index], first removing any already present by key. */
    suspend fun insertAt(index: Int, items: List<T>) {
        var insertIndex = index
        items.forEach { item ->
            val key = keyOf(item)
            liveItems.indexOfFirst { keyOf(it) == key }.takeIf { it >= 0 }?.let {
                removeAt(it)
                if (it < insertIndex) insertIndex--
            }
        }

        liveItems.addAll(insertIndex, items)
        state.set { copy(size = liveItems.size) }
        changes.emit(ListChange.Insert(insertIndex, items))
    }

    suspend fun insertAt(index: Int, item: T) = insertAt(index, listOf(item))
    suspend fun add(item: T) = insertAt(liveItems.size, listOf(item))
    suspend fun add(items: List<T>) = insertAt(liveItems.size, items)

    /** Inserts [item] before the item with [key]. Does nothing when there is none. */
    suspend fun insertBefore(key: K, item: T) {
        val index = liveItems.indexOfFirst { keyOf(it) == key }
        if (index >= 0) insertAt(index, item)
    }

    /** Inserts [item] after the item with [key]. Does nothing when there is none. */
    suspend fun insertAfter(key: K, item: T) {
        val index = liveItems.indexOfFirst { keyOf(it) == key }
        if (index >= 0) insertAt(index + 1, item)
    }

    /** Inserts [item] before the first item matching [predicate]. Does nothing when there is none. */
    suspend fun insertBeforeFirst(item: T, predicate: (T) -> Boolean) {
        val index = liveItems.indexOfFirst(predicate)
        if (index >= 0) insertAt(index, item)
    }

    /** Inserts [item] after the first item matching [predicate]. Does nothing when there is none. */
    suspend fun insertAfterFirst(item: T, predicate: (T) -> Boolean) {
        val index = liveItems.indexOfFirst(predicate)
        if (index >= 0) insertAt(index + 1, item)
    }

    suspend fun replaceAt(index: Int, item: T) {
        liveItems[index] = item
        changes.emit(ListChange.Replace(index, item))
    }

    /** Replaces the item with the same key as [item]. Does nothing when there is none. */
    suspend fun replace(item: T) {
        val key = keyOf(item)
        val index = liveItems.indexOfFirst { keyOf(it) == key }
        if (index >= 0) replaceAt(index, item)
    }

    /** Replaces the first item matching [predicate]. Does nothing when there is none. */
    suspend fun replaceFirst(item: T, predicate: (T) -> Boolean) {
        val index = liveItems.indexOfFirst(predicate)
        if (index >= 0) replaceAt(index, item)
    }

    suspend fun removeAt(index: Int) {
        liveItems.removeAt(index)
        state.set { copy(size = liveItems.size) }
        changes.emit(ListChange.Remove(index, 1))
    }

    suspend fun removeAt(index: Int, count: Int) {
        liveItems.subList(index, index + count).clear()
        state.set { copy(size = liveItems.size) }
        changes.emit(ListChange.Remove(index, count))
    }

    /** Removes the item with [key]. Does nothing when there is none. */
    suspend fun remove(key: K) {
        val index = liveItems.indexOfFirst { keyOf(it) == key }
        if (index >= 0) removeAt(index)
    }

    /** Removes the first item matching [predicate]. Does nothing when there is none. */
    suspend fun removeFirst(predicate: (T) -> Boolean) {
        val index = liveItems.indexOfFirst(predicate)
        if (index >= 0) removeAt(index)
    }

    suspend fun clear() {
        liveItems.clear()
        state.set { copy(size = 0) }
        changes.emit(ListChange.Clear)
    }
}

/** The observable state of a [LiveList]. */
data class LiveListState(
    val size: Int,
)

/** A change to a [LiveList]. */
sealed interface ListChange<out T> {
    data class Insert<T>(val index: Int, val items: List<T>) : ListChange<T>
    data class Replace<T>(val index: Int, val item: T) : ListChange<T>
    data class Remove(val index: Int, val count: Int) : ListChange<Nothing>
    data object Clear : ListChange<Nothing>
}