package kampfire.utils

/** This value, or an [IllegalArgumentException] with [lazyMessage] when it is `null`. */
fun <T : Any> T?.requireNotNull(lazyMessage: () -> String = { "Required value was null." }): T =
    requireNotNull(this, lazyMessage)