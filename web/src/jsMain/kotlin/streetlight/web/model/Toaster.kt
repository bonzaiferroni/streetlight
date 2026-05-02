package streetlight.web.model

import koala.dom.UIMessage
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class Toaster(
    private val scope: CoroutineScope
) {
    private val state = storeOf(ToasterState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val messagesFlow = stateFlow.mapDistinct { it.messages }

    fun toast(message: String) = toast(UIMessage(message))

    fun toast(message: UIMessage) {
        state.set { it.copy(messages = it.messages + message)}
        scope.launch {
            delay(ToastDelay)
            state.set { it.copy(messages = it.messages - message) }
        }
    }
}

data class ToasterState(
    val messages: List<UIMessage> = emptyList()
)

private val ToastDelay = 10.seconds