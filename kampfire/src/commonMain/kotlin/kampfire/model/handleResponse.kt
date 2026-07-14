package kampfire.model

fun <T> Outcome<T>?.getDataOrNull() = when (this) {
    is Ok -> this.data
    is Problem -> null.also { println("Problem: ${this.message}") }
    null -> null.also { println("Response was null") }
}

fun <T> Outcome<T>?.handleResponse(
    onMessage: (String) -> Unit,
    okMessage: String? = null,
) = handleResponse(onMessage, okMessage) { it }

fun <T1, T2> Outcome<T1>?.handleResponse(
    onMessage: (String) -> Unit,
    okMessage: String? = null,
    block: (T1) -> T2
): T2? = when (this) {
    is Ok -> {
        okMessage?.let {
            onMessage(it)
        }
        block(data)
    }

    is Problem -> {
        onMessage(message)
        null
    }

    null -> {
        onMessage("No response.")
        null
    }
}

fun <T> Outcome<T>.handleOutcome(
    onMessage: (String) -> Unit,
    okMessage: String? = null,
) = handleOutcome(onMessage, okMessage) { it }

fun <T1, T2> Outcome<T1>.handleOutcome(
    onMessage: (String) -> Unit,
    okMessage: String? = null,
    block: (T1) -> T2
): T2? = when (this) {
    is Ok -> {
        okMessage?.let {
            onMessage(it)
        }
        block(data)
    }

    is Problem -> {
        onMessage(message)
        null
    }
}

