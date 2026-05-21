package koala.dom

import kampfire.model.Problem
import koala.model.Store
import koala.model.storeOf

data class UIMessage(
    val text: String,
    val type: UIMessageType = UIMessageType.Info,
)

enum class UIMessageType {
    Info,
    Warning,
    Error,
    Working,
}

fun Store<UIMessage?>.set(text: String, type: UIMessageType = UIMessageType.Info) = set { UIMessage(text, type) }

fun Store<UIMessage?>.set(problem: Problem<*>?) = set { UIMessage(problem?.message ?: "Something went wrong.") }

fun Store<UIMessage?>.clear() = set { null }

fun messageStore(value: UIMessage? = null) = storeOf(value)