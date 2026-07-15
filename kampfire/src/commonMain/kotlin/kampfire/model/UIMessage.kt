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

interface MessageReceiver {
    fun receive(text: String?)
    fun receive(message: UIMessage)
    fun receive(problem: Problem)
}

object PrintLnReceiver: MessageReceiver {
    override fun receive(text: String?) {
        text?.let {
            println(it)
        }
    }

    override fun receive(message: UIMessage) {
        println(message.text)
    }

    override fun receive(problem: Problem) {
        println("Problem: ${problem.message}")
    }
}