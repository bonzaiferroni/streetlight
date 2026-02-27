package koala.model

import koala.dom.RenderContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class Flower<T>(
    replay: Int = 1,
    private val fetch: suspend () -> T?,
    private val context: RenderContext
) {
    private val _flow = MutableSharedFlow<T>(replay)
    val flow: Flow<T> = _flow

    fun refresh() {
        console.log("refreshing")
        context.renderScope.launch {
            // todo: handle retries
            val value = fetch() ?: return@launch
            _flow.emit(value)
        }
    }
}

fun <T> RenderContext.flowerOf(
    initialRefresh: Boolean = true,
    replay: Int = 1,
    fetch: suspend () -> T?
): Flower<T> = Flower(replay, fetch, this).also { if (initialRefresh) it.refresh() }