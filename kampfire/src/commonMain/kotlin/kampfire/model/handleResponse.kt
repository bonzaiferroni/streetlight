package kampfire.model

fun <T> Outcome<T>?.getDataOrNull() = when (this) {
    is Ok -> this.data
    is Problem -> null.also { println("Problem: ${this.message}") }
    null -> null.also { println("Response was null") }
}

fun <T> Outcome<T>?.handleResponse(
    receiver: MessageReceiver,
    okReceiver: MessageReceiver = receiver,
) = handleResponse(receiver, okReceiver) { it }

fun <T1, T2> Outcome<T1>?.handleResponse(
    receiver: MessageReceiver,
    okReceiver: MessageReceiver = receiver,
    block: (T1) -> T2
): T2? = when (this) {
    is Ok -> {
        message?.let {
            okReceiver.receive(UIMessage(it, UIMessageType.Success))
        }
        block(data)
    }

    is Problem -> {
        receiver.receive(this)
        null
    }

    null -> {
        receiver.receive(Problem("No response."))
        null
    }
}

fun <T> Outcome<T>.handleOutcome(
    receiver: MessageReceiver,
    okReceiver: MessageReceiver = receiver,
) = handleOutcome(receiver, okReceiver) { it }

fun <T1, T2> Outcome<T1>.handleOutcome(
    receiver: MessageReceiver,
    okReceiver: MessageReceiver = receiver,
    block: (T1) -> T2
): T2? = when (this) {
    is Ok -> {
        message?.let {
            okReceiver.receive(it)
        }
        block(data)
    }

    is Problem -> {
        receiver.receive(this)
        null
    }
}

