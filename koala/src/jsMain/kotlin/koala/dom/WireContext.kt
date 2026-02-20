package koala.dom

import koala.model.ModelState
import koala.model.stateOf

class WireContext<T>(
    val state: ModelState<T>,
    context: RenderContext
): RenderContext by context

fun <T> RenderContext.wireContextOf(
    initialState: T
): WireContext<T> {
    val state = stateOf(initialState)
    return WireContext(state, this)
}

fun <T> RenderContext.wireState(
    initialState: T,
    block: WireContext<T>.() -> Unit
) {
    val context = wireContextOf(initialState)
    context.block()
}

fun <T> RenderContext.wireTo(
    state: ModelState<T>,
    block: WireContext<T>.() -> Unit
) {
    val context = WireContext(state, this)
    context.block()
}