package kampfire.model

fun <T> Response<T>?.getDataOrNull() = when (this) {
    is Ok -> this.data
    is Problem -> null.also { println("Problem: ${this.message}") }
    null -> null.also { println("Response was null") }
}

fun <T> Response<T>?.handleResponse(
    onMessage: (String) -> Unit,
    okMessage: String? = null,
) = handleResponse(onMessage, okMessage) { it }

fun <T1, T2> Response<T1>?.handleResponse(
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

fun <T> Response<T>?.isOk() = when (this) {
    is Ok -> true
    else -> false
}

