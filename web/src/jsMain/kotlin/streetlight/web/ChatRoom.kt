package streetlight.web

import koala.model.BrowserModel
import koala.model.mapDistinct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.WebSocket

class ChatRoom(
    private val scope: CoroutineScope,
    private val client: ApiClient
): BrowserModel<ChatRoomState>(ChatRoomState(), scope) {

    val messagesFlow = stateFlow.mapDistinct { it.messages }
    val messageFlow = stateFlow.mapDistinct { it.message }

    private var socket: ChatSocket? = null

    fun setIsActive(value: Boolean) {
        if (value) {
            if (socket == null) {
                val socket = client.connectChat(scope)
                viewModelScope.launch {
                    socket.messageFlow.collect { message ->
                        setState { it.copy(messages = stateNow.messages + message) }
                    }
                }
                this.socket = socket
            }
        }
        setState { it.copy(isActive = value) }
    }

    fun setMessage(value: String) {
        setState { it.copy(message = value) }
    }

    fun sendMessage() {
        val socket = socket ?: return
        socket.send(stateNow.message)
        setState { it.copy(message = "") }
    }
}

data class ChatRoomState(
    val messages: List<String> = emptyList(),
    val message: String = "",
    val isActive: Boolean = false,
)