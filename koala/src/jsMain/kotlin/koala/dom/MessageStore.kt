package koala.dom

import kampfire.model.Messenger
import kampfire.model.Problem
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import kampfire.model.Store
import koala.model.dedup

/** A [Messenger] that holds one message, replaced on each delivery, for a [messageBox] to show. */
class MessageStore(value: UIMessage? = null): Store<UIMessage?>(value), Messenger {
    /** Whether the message held is a [UIMessageType.Working] message. */
    val isWorkingFlow = flow.dedup { it?.let { it.messageType == UIMessageType.Working } ?: false }

    /** Holds [text] as a message of [messageType], or clears it when [text] is `null`. */
    fun set(text: String?, messageType: UIMessageType = UIMessageType.Info) = set {
        text?.let { UIMessage(it, messageType)}
    }

    /** Holds [text] as a working message, or a plain one. */
    fun set(text: String, isWorking: Boolean) = set(UIMessage(text, if (isWorking) UIMessageType.Working else UIMessageType.Info))

    override fun deliver(text: String) = set(text)
    override fun deliver(problem: Problem) = set { UIMessage(problem.message, messageType = UIMessageType.Error) }
    override fun deliver(message: UIMessage) = set { message }

    fun clear() = set { null }
}