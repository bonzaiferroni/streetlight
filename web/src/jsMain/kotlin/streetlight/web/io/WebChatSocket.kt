package streetlight.web.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import streetlight.model.data.ChatMessage
import web.events.EventHandler
import web.sockets.WebSocket

class WebChatSocket(
    private val socket: WebSocket,
    private val scope: CoroutineScope
) {
    private val _messageFlow = MutableSharedFlow<ChatMessage>(
        replay = 20,
        extraBufferCapacity = 64
    )
    val messageFlow: Flow<ChatMessage> = _messageFlow

    init {
        socket.onmessage = EventHandler({ event ->
            scope.launch {
                val data = event.data
                if (data is String) {
                    val message = data.decode()
                    if (message != null) {
                        _messageFlow.emit(message)
                    } else {
                        console.log("invalid msg: $data")
                    }
                }
            }
        })
    }

    fun send(message: ChatMessage) {
        socket.send(message.encode())
    }
}

private fun ChatMessage.encode() = Json.encodeToString(this)

private fun String.decode(): ChatMessage? {
    return try {
        Json.decodeFromString(this)
    } catch(e: Exception) {
        null
    }
}