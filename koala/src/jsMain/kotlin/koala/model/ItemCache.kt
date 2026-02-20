package koala.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ItemCache<T>(
    private val scope: CoroutineScope,
    private val provideInitialItems: suspend () -> List<T>?
){
    private val _flow = MutableSharedFlow<List<T>>()
    private val items = mutableListOf<T>()
    private var isInitialized = false

    val flow: Flow<List<T>> get() {
        if (!isInitialized) {
            isInitialized = true
            scope.launch {
                val initialItems = provideInitialItems() ?: error("todo: handle null initial items")
                items.addAll(initialItems)
                _flow.emit(items)
            }
        }
        return _flow
    }

    fun addItem(item: T) {
        items.add(item)
        scope.launch {
            _flow.emit(items)
        }
    }

    fun clear() {
        items.clear()
        isInitialized = false
    }
}