package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.Problem
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.model.dedup
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class Toaster(
    private val scope: CoroutineScope
): Messenger {
    private val state = storeOf(ToasterState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val messagesFlow = stateFlow.dedup { it.messages }

    fun toast(message: String, messageType: UIMessageType = UIMessageType.Info) =
        toast(UIMessage(message, messageType))

    fun toast(message: UIMessage) {
        state.set { copy(messages = messages + message)}
        scope.launch {
            delay(ToastDelay)
            state.set { copy(messages = stateNow.messages - message) }
        }
    }

    override fun deliver(text: String) { toast(text, UIMessageType.Info) }
    override fun deliver(problem: Problem) = toast(problem.message, UIMessageType.Error)
    override fun deliver(message: UIMessage) = toast(message)
}

data class ToasterState(
    val messages: List<UIMessage> = emptyList()
)

private val ToastDelay = 10.seconds