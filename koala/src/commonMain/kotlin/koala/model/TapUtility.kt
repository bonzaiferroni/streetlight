package koala.model

fun MutableTap<Boolean>.toggle() {
    update { !it }
}

fun <T> MutableTap<List<T>>.insertAt(index: Int, item: T) {
    update { list ->
        val mutableList = list.toMutableList()
        mutableList.add(index, item)
        mutableList
    }
}