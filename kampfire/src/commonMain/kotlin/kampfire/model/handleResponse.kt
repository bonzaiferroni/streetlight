package kampfire.model

//// td: deprecate in favor of toDataOr
//fun <T> Outcome<T>.handleResponse(
//    messenger: Messenger,
//    defaultOkMessage: String? = null,
//    okMessenger: Messenger = messenger,
//) = handleResponse(messenger, defaultOkMessage, okMessenger) { it }
//
//// td: deprecate in favor of toDataOr
//fun <T1, T2> Outcome<T1>.handleResponse(
//    messenger: Messenger,
//    defaultOkMessage: String? = null,
//    okMessenger: Messenger = messenger,
//    block: (T1) -> T2
//): T2? = when (this) {
//    is Ok -> {
//        val deliveredMessage = message ?: defaultOkMessage
//        deliveredMessage?.let {
//            okMessenger.deliver(UIMessage(it, UIMessageType.Success))
//        }
//        block(data)
//    }
//
//    is Problem -> {
//        messenger.deliver(this)
//        null
//    }
//}

// td: deprecate in favor of toDataOr
fun <T> Outcome<T>.handleOutcome(
    messenger: Messenger,
    okMessenger: Messenger = messenger,
) = handleOutcome(messenger, okMessenger) { it }

// td: deprecate in favor of toDataOr
fun <T1, T2> Outcome<T1>.handleOutcome(
    messenger: Messenger,
    okMessenger: Messenger = messenger,
    block: (T1) -> T2
): T2? = when (this) {
    is Ok -> {
        message?.let {
            okMessenger.deliver(it)
        }
        block(data)
    }

    is Problem -> {
        messenger.deliver(this)
        null
    }
}