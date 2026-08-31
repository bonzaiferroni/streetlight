package kampfire.model

fun MutableTap<Boolean>.toggle() {
    update { !it }
}

fun MutableTap<Boolean>.setTrue() {
    set(true)
}

fun MutableTap<Boolean>.setFalse() {
    set(false)
}

fun <T> MutableTap<List<T>>.insertAt(index: Int, item: T) {
    update { list ->
        val mutableList = list.toMutableList()
        mutableList.add(index, item)
        mutableList
    }
}

fun <T> MutableTap<List<T>>.removeAt(index: Int) {
    set { filterIndexed { i, _ -> i != index } }
}

fun <T> MutableTap<List<T>>.replaceAt(index: Int, item: T) {
    update { list ->
        val mutableList = list.toMutableList()
        mutableList[index] = item
        mutableList
    }
}

fun <T> MutableTap<List<T>>.append(item: T) {
    update { list ->
        list + item
    }
}