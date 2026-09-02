package streetlight.web.io

import koala.utils.jsonConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import web.events.EventHandler
import web.events.EventType
import web.events.addEventListener
import web.history.PageTransitionEvent
import web.sse.EventSource
import web.window.window

class SSEClient<Message>(
    private val scope: CoroutineScope,
    private val messageSerializer: KSerializer<Message>,
    private val provideSource: suspend () -> EventSource,
) {
    private val _itemFlow = MutableSharedFlow<Message>(0, 8)
    val messageFlow: Flow<Message> = _itemFlow

    private var source: EventSource? = null

    init {
        window.addEventListener(EventType<PageTransitionEvent>("pagehide"), {
            source?.close()
        })
    }

    fun connect() {
        scope.launch {
            val source = provideSource().also { source = it }
            source.onmessage = EventHandler { event ->
                scope.launch {
                    val message = decode(event.data as String) ?: return@launch
                    _itemFlow.emit(message)
                }
            }
        }
    }

    fun disconnect() {
        source?.let {
            it.close()
            source = null
        }
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