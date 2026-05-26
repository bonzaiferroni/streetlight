package koala.dom

import kampfire.model.Problem
import koala.model.Store
import koala.model.storeOf

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