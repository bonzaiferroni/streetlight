package streetlight.web.io

import koala.utils.jsonConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.w3c.dom.EventSource

class SSEClient<Message>(
    private val scope: CoroutineScope,
    private val messageSerializer: KSerializer<Message>,
    private val provideSource: suspend () -> EventSource,
) {
    private val _itemFlow = MutableSharedFlow<Message>(0, 8)
    val messageFlow: Flow<Message> = _itemFlow

    private var source: EventSource? = null

    fun connect() {
        scope.launch {
            val source = provideSource().also { source = it }
            source.onmessage = { event ->
                scope.launch {
                    val message = decode(event.data as String) ?: return@launch
                    _itemFlow.emit(message)
                }
            }
        }
    }

    fun disconnect() {
        val source = source ?: error("source not connected: ${this::class.simpleName}")
        source.close()
        this.source = null
    }

    private fun decode(text: String): Message? = try {
        jsonConfig.decodeFromString(messageSerializer, text)
    } catch (e: Exception) {
        console.log(e.message)
        null
    }
}

inline fun <reified Message> sseClientOf(
    scope: CoroutineScope,
    noinline provideSource: suspend () -> EventSource,
): SSEClient<Message> = SSEClient(scope, serializer(), provideSource)