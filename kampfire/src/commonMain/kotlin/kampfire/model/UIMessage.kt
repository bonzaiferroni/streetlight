package kampfire.model

/** A message shown to the user, of a [messageType] that decides how it looks. */
data class UIMessage(
    val text: String,
    val messageType: UIMessageType = UIMessageType.Info,
)

/** The kind of a [UIMessage]. */
enum class UIMessageType {
    Info,
    Error,
    Working,
    Success,
}

/**
 * Delivers messages to the user. The caller decides what a message says; the implementation decides where it
 * appears.
 */
interface Messenger {
    fun deliver(text: String)
    fun deliver(message: UIMessage)
    fun deliver(problem: Problem)

    /** Delivers a [UIMessageType.Working] message, the first beat of an interaction that reaches the server. */
    fun deliverSending(text: String = SendingMessage) = deliver(UIMessage(text, UIMessageType.Working))
    /** Delivers a [UIMessageType.Success] message. */
    fun deliverSuccess(text: String) = deliver(UIMessage(text, UIMessageType.Success))

    companion object {
        val SendingMessage = "Sending..."
    }
}

/** A [Messenger] that prints to standard output, for code with no user in front of it. */
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