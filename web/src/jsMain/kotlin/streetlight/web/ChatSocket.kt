package streetlight.web

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.w3c.dom.WebSocket

class ChatSocket(
    private val socket: WebSocket,
    private val scope: CoroutineScope
) {
    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: Flow<String> = _messageFlow

    init {
        socket.onmessage = { event ->
            scope.launch {
                val data = event.data
                if (data is String) {
                    _messageFlow.emit(data)
                }
            }
        }
    }

    fun send(message: String) {
        console.log(message)
        socket.send(message)
    }
}