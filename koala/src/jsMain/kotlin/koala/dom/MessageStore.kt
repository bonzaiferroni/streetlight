package koala.dom

import kampfire.model.Messenger
import kampfire.model.Problem
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.model.Store
import koala.model.dedup

class MessageStore(value: UIMessage? = null): Store<UIMessage?>(value), Messenger {
    val isWorkingFlow = flow.dedup { it?.let { it.messageType == UIMessageType.Working } ?: false }

    fun set(text: String?, messageType: UIMessageType = UIMessageType.Info) = setValue {
        text?.let { UIMessage(it, messageType)}
    }

    fun set(text: String, isWorking: Boolean) = set(UIMessage(text, if (isWorking) UIMessageType.Working else UIMessageType.Info))

    override fun receive(text: String?) = set(text)
    override fun receive(problem: Problem) = setValue { UIMessage(problem.message, messageType = UIMessageType.Error) }
    override fun receive(message: UIMessage) = setValue { message }

    fun clear() = setValue { null }
}