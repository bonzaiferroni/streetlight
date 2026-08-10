package koala.model

fun MutableField<Boolean>.toggle() {
    update { !it }
}

fun <T> MutableField<List<T>>.insertAt(index: Int, item: T) {
    update { list ->
        val mutableList = list.toMutableList()
        mutableList.add(index, item)
        mutableList
    }
}