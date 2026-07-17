package streetlight.web.model

import kampfire.model.MessageReceiver
import kampfire.model.Problem
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.model.tap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class Toaster(
    private val scope: CoroutineScope
): MessageReceiver {
    private val state = storeOf(ToasterState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val messagesFlow = stateFlow.tap { it.messages }

    fun toast(message: String, messageType: UIMessageType = UIMessageType.Info) =
        toast(UIMessage(message, messageType))

    fun toast(message: UIMessage) {
        state.set { it.copy(messages = it.messages + message)}
        scope.launch {
            delay(ToastDelay)
            state.set { it.copy(messages = stateNow.messages - message) }
        }
    }

    override fun receive(text: String?) { text?.let { toast(text, UIMessageType.Info) } }
    override fun receive(problem: Problem) = toast(problem.message, UIMessageType.Error)
    override fun receive(message: UIMessage) = toast(message)
}

data class ToasterState(
    val messages: List<UIMessage> = emptyList()
)

private val ToastDelay = 10.seconds