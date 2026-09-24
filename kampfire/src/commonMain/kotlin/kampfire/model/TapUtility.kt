package kampfire.model

/** Inverts the value. */
fun MutableTap<Boolean>.toggle() {
    update { !it }
}

fun MutableTap<Boolean>.setTrue() {
    set(true)
}

fun MutableTap<Boolean>.setFalse() {
    set(false)
}

/** Inserts [item] into the list at [index]. */
fun <T> MutableTap<List<T>>.insertAt(index: Int, item: T) {
    update { list ->
        val mutableList = list.toMutableList()
        mutableList.add(index, item)
        mutableList
    }
}

/** Removes the item at [index]. */
fun <T> MutableTap<List<T>>.removeAt(index: Int) {
    set { filterIndexed { i, _ -> i != index } }
}

/** Replaces the item at [index] with [item]. */
fun <T> MutableTap<List<T>>.replaceAt(index: Int, item: T) {
    update { list ->
        val mutableList = list.toMutableList()
        mutableList[index] = item
        mutableList
    }
}

/** Adds [item] to the end of the list. */
fun <T> MutableTap<List<T>>.append(item: T) {
    update { list ->
        list + item
    }
}

/** A lens onto the first item matching [predicate]. Writing through it replaces every matching item. */
fun <T> MutableTap<List<T>>.mutableTapFirstBy(predicate: (T) -> Boolean) = mutableTapOf({ it.first(predicate) }) { value ->
    map { if (predicate(it)) value else it }
}