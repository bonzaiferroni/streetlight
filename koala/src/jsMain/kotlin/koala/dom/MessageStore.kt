package koala.dom

import kampfire.model.MessageReceiver
import kampfire.model.Problem
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.model.Store
import koala.model.tap

class MessageStore(value: UIMessage? = null): Store<UIMessage?>(value), MessageReceiver {
    val isWorkingFlow = flow.tap { it?.let { it.messageType == UIMessageType.Working } ?: false }

    fun set(text: String?, messageType: UIMessageType = UIMessageType.Info) = set {
        text?.let { UIMessage(it, messageType)}
    }

    fun set(text: String, isWorking: Boolean) = set(UIMessage(text, if (isWorking) UIMessageType.Working else UIMessageType.Info))

    override fun receive(text: String?) = set(text)
    override fun receive(problem: Problem) = set { UIMessage(problem.message, messageType = UIMessageType.Error) }
    override fun receive(message: UIMessage) = set { message }

    fun clear() = set { null }
}