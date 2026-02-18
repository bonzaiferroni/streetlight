package koala.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Deprecated("Use ModelState")
open class BrowserModel<T>(
    initialValue: T,
    val scope: CoroutineScope
) {
    private val state = MutableStateFlow(initialValue)

    val stateFlow = state.asStateFlow()
    val stateNow get() = stateFlow.value

    protected fun setState(setter: (T) -> T) {
        state.value = setter(stateFlow.value)
    }
}