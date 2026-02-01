package koala.dom

data class UIMessage(
    val type: UIMessageType,
    val message: String,
)

enum class UIMessageType {
    Info,
    Warning,
    Error
}