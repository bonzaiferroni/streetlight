package koala.dom

import koala.model.ModelState
import koala.model.stateOf

class StateRenderContext <T>(
    val state: ModelState<T>,
    val render: RenderContext
)

fun <T> RenderContext.stateContextOf(
    initialState: T
): StateRenderContext<T> {
    val state = stateOf(initialState)
    return StateRenderContext(state, this)
}

fun <T> RenderContext.renderState(
    initialState: T,
    block: StateRenderContext<T>.() -> Unit
) {
    val context = stateContextOf(initialState)
    context.block()
}