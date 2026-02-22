package koala.dom

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

fun Store<UIMessage>.set(text: String, type: UIMessageType = UIMessageType.Info) = set { UIMessage(text, type) }

fun Store<UIMessage>.clear() = set { UIMessage() }