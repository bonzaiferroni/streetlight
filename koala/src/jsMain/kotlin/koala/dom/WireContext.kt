package koala.dom

import koala.html.AppRoute
import koala.model.Store
import koala.model.Portal
import koala.model.storeOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WireContext<T>(
    val state: Store<T>,
    context: ScopedDOM
): ScopedDOM by context

fun <T> ScopedDOM.wireContextOf(
    initialState: T
): WireContext<T> {
    val state = storeOf(initialState)
    return WireContext(state, this)
}

fun <T> ScopedDOM.wireState(
    initialState: T,
    block: WireContext<T>.() -> Unit
) {
    val context = wireContextOf(initialState)
    context.block()
}

fun <T> ScopedDOM.wireTo(
    state: Store<T>,
    block: WireContext<T>.() -> Unit
) {
    val context = WireContext(state, this)
    context.block()
}

inline fun <reified Route: AppRoute, Data> ScopedDOM.wireRouteTo(
    portal: Portal,
    initialState: Data,
    crossinline provideData: suspend (Route) -> Data?,
    crossinline block: WireContext<Data>.() -> Unit
) {
    val context = wireContextOf(initialState)

    context.block()

    renderScope.launch {
        portal.routeFlowOf<Route>().map(provideData).collect {
            context.state.set { it }
        }
    }
}