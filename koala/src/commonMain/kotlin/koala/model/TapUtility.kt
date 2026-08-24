package koala.model

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