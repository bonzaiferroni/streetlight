package streetlight.agent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

open class AgentModel<T>(
    initialValue: T,
    val thread: CoroutineScope
) {
    private val state = MutableStateFlow(initialValue)

    val stateFlow = state.asStateFlow()
    val stateNow get() = stateFlow.value

    protected fun setState(setter: (T) -> T) {
        state.value = setter(stateFlow.value)
    }
}