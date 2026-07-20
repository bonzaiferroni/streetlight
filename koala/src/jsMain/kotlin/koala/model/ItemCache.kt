package koala.model

import kampfire.model.Messenger
import kampfire.model.Outcome
import kampfire.model.handleResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.collections.List

class ItemCache<Item, ItemId>(
    private val scope: CoroutineScope,
    private val onError: Messenger,
    private val provideId: (Item) -> ItemId,
    private val provideInitialItems: suspend () -> Outcome<List<Item>>
){
    private val _flow = MutableSharedFlow<List<Item>>(replay = 8)
    private val items = mutableListOf<Item>()
    private var isInitialized = false

    val flow: Flow<List<Item>> get() {
        if (!isInitialized) {
            scope.launch {
                initializeItems()
            }
        }
        return _flow
    }

    fun addItem(item: Item) {
        val id = provideId(item)
        items.removeAll { provideId(it) == id }
        items.add(item)
        scope.launch {
            _flow.emit(items)
        }
    }

    fun getCachedItem(id: ItemId) = items.firstOrNull { provideId(it) == id }

    suspend fun getItem(id: ItemId): Item? {
        initializeItems()
        return items.firstOrNull { provideId(it) == id }
    }

    fun clear() {
        items.clear()
        isInitialized = false
    }

    suspend fun getItems(): List<Item> {
        initializeItems()
        return items
    }

    private suspend fun initializeItems() {
        if (isInitialized) return
        isInitialized = true

        val initialItems = provideInitialItems().handleResponse(onError) ?: return
        items.addAll(initialItems)
        _flow.emit(items)
    }
}