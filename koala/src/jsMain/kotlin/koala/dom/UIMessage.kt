package koala.dom

import kampfire.model.Problem
import koala.model.Store

data class UIMessage(
    val text: String? = null,
    val type: UIMessageType = UIMessageType.Info,
)

enum class UIMessageType {
    Info,
    Warning,
    Error
}

fun Store<UIMessage?>.set(text: String, type: UIMessageType = UIMessageType.Info) = set { UIMessage(text, type) }

fun Store<UIMessage?>.set(problem: Problem<*>?) = set { UIMessage(problem?.message ?: "No response.") }

fun Store<UIMessage?>.clear() = set { null }