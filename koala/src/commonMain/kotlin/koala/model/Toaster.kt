package koala.model

import kampfire.model.Messenger
import kampfire.model.Problem
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/** Shows each message as a toast for a while, an error for longer, kept while held and gone once dismissed. */
class Toaster(
    private val scope: CoroutineScope
): Messenger {
    private val state = storeOf(ToasterState())
    private val timers = mutableMapOf<Toast, Job>()
    private var nextId = 0L
    val stateNow get() = state.now
    val stateFlow = state.flow

    val toastsState = state.tapOf { it.toasts }

    fun toast(message: String, messageType: UIMessageType = UIMessageType.Info) =
        toast(UIMessage(message, messageType))

    fun toast(message: UIMessage) {
        val toast = Toast(nextId++, message)
        state.set { copy(toasts = toasts + toast) }
        expire(toast)
    }

    fun dismiss(toast: Toast) {
        timers.remove(toast)?.cancel()
        remove(toast)
    }

    /** Keeps [toast] until it is [released][release]. */
    fun hold(toast: Toast) {
        timers.remove(toast)?.cancel()
    }

    /** Lets a held [toast] fade after its full time. */
    fun release(toast: Toast) {
        if (toast in stateNow.toasts && toast !in timers) expire(toast)
    }

    override fun deliver(text: String) { toast(text, UIMessageType.Info) }
    override fun deliver(problem: Problem) = toast(problem.message, UIMessageType.Error)
    override fun deliver(message: UIMessage) = toast(message)

    private fun expire(toast: Toast) {
        val time = if (toast.message.messageType == UIMessageType.Error) ErrorToastTime else ToastTime
        timers[toast] = scope.launch {
            delay(time)
            timers.remove(toast)
            remove(toast)
        }
    }

    private fun remove(toast: Toast) = state.set { copy(toasts = toasts - toast) }
}

data class ToasterState(
    val toasts: List<Toast> = emptyList()
)

/** A [message] shown by a [Toaster], told from another of the same text by its [id]. */
data class Toast(val id: Long, val message: UIMessage)

private val ToastTime = 10.seconds
private val ErrorToastTime = 20.seconds
