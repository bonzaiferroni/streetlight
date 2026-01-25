package streetlight.web

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

open class BrowserModel<T>(
    initialValue: T,
    val viewModelScope: CoroutineScope
) {
    protected val _state = MutableStateFlow(initialValue)

    val stateNow get() = _state.value
    val stateFlow get() = _state.asStateFlow()

    protected fun setState(setter: (T) -> T) {
        _state.value = setter(_state.value)
    }
}