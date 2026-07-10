package kampfire.utils

fun <T : Any> T?.requireNotNull(lazyMessage: () -> String = { "Required value was null." }): T =
    requireNotNull(this, lazyMessage)