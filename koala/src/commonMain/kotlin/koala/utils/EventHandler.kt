package koala.utils

interface EventHandler {
    operator fun invoke(block: () -> Unit): () -> Unit
}

class MutableEventHandler : EventHandler {
    private val handlers = mutableSetOf<() -> Unit>()

    override operator fun invoke(block: () -> Unit): () -> Unit {
        handlers += block
        return { handlers -= block }
    }

    fun emit() {
        handlers.forEach { it() }
    }
}