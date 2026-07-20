package kampfire.model

@Deprecated("use handleResponse with PrintLnMessenger")
fun <T> Outcome<T>.getDataOrNull() = when (this) {
    is Ok -> this.data
    is Problem -> null.also { println("Problem: ${this.message}") }
}

fun <T> Outcome<T>.handleResponse(
    messenger: Messenger,
    okMessenger: Messenger = messenger,
) = handleResponse(messenger, okMessenger) { it }

fun <T1, T2> Outcome<T1>.handleResponse(
    messenger: Messenger,
    okMessenger: Messenger = messenger,
    block: (T1) -> T2
): T2? = when (this) {
    is Ok -> {
        message?.let {
            okMessenger.receive(UIMessage(it, UIMessageType.Success))
        }
        block(data)
    }

    is Problem -> {
        messenger.receive(this)
        null
    }
}

fun <T> Outcome<T>.handleOutcome(
    messenger: Messenger,
    okMessenger: Messenger = messenger,
) = handleOutcome(messenger, okMessenger) { it }

fun <T1, T2> Outcome<T1>.handleOutcome(
    messenger: Messenger,
    okMessenger: Messenger = messenger,
    block: (T1) -> T2
): T2? = when (this) {
    is Ok -> {
        message?.let {
            okMessenger.receive(it)
        }
        block(data)
    }

    is Problem -> {
        messenger.receive(this)
        null
    }
}

