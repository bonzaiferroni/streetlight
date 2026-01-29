package streetlight.web

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BrowserModel<T>(
    initialValue: T,
    val viewModelScope: CoroutineScope
) {
    val state: StateFlow<T> field = MutableStateFlow(initialValue)
    val stateNow get() = state.value

    protected fun setState(setter: (T) -> T) {
        state.value = setter(state.value)
    }
}