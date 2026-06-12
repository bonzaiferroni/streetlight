package koala.model

import koala.dom.AppScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class Flower<T>(
    replay: Int = 1,
    private val fetch: suspend () -> T?,
    private val context: AppScope
) {
    private val _flow = MutableSharedFlow<T>(replay)
    val flow: Flow<T> = _flow

    fun refresh() {
        console.log("refreshing")
        context.parentScope.launch {
            // todo: handle retries
            val value = fetch() ?: return@launch
            _flow.emit(value)
        }
    }
}

fun <T> AppScope.flowerOf(
    initialRefresh: Boolean = true,
    replay: Int = 1,
    fetch: suspend () -> T?
): Flower<T> = Flower(replay, fetch, this).also { if (initialRefresh) it.refresh() }