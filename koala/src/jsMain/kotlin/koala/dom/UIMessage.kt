package koala.dom

data class UIMessage(
    val text: String,
    val isWorking: Boolean = false,
    val type: UIMessageType = UIMessageType.Info,
)

enum class UIMessageType {
    Info,
    Warning,
    Error,
    Working,
}