package kampfire.model

data class UIMessage(
    val text: String,
    val messageType: UIMessageType = UIMessageType.Info,
)

enum class UIMessageType {
    Info,
    Error,
    Working,
    Success,
}

interface Messenger {
    fun deliver(text: String)
    fun deliver(message: UIMessage)
    fun deliver(problem: Problem)

    fun deliverSending() = deliver(UIMessage("Sending...", UIMessageType.Working))
    fun deliverSuccess(text: String) = deliver(UIMessage(text, UIMessageType.Success))
}

object PrintLnMessenger: Messenger {
    override fun deliver(text: String) {
        println(text)
    }

    override fun deliver(message: UIMessage) {
        println(message.text)
    }

    override fun deliver(problem: Problem) {
        println("Problem: ${problem.message}")
    }
}