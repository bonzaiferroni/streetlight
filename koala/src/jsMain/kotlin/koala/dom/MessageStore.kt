package koala.dom

import kampfire.model.Problem
import koala.model.Store
import koala.model.tap

class MessageStore(value: UIMessage? = null): Store<UIMessage?>(value) {
    val isWorkingFlow = flow.tap { it?.isWorking ?: false }

    fun set(problem: Problem?) = set { UIMessage(problem?.message ?: "Something went wrong.") }

    fun set(text: String?, isWorking: Boolean = false, type: UIMessageType = UIMessageType.Info) = set {
        text?.let { UIMessage(it, isWorking, type)}
    }

    fun clear() = set { null }
}