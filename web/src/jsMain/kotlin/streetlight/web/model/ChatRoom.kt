package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Clock
import streetlight.model.data.ChatMessage
import streetlight.web.io.ApiClient
import streetlight.web.io.WebChatSocket

class ChatRoom(
    private val scope: CoroutineScope,
    private val client: ApiClient
) {
    private val state = storeOf(ChatRoomState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    val messagesFlow = stateFlow.mapDistinct { it.messages }
    val sendFlow = stateFlow.mapDistinct { it.message }

    private var socket: WebChatSocket? = null

    fun setIsActive(value: Boolean) {
        if (value) {
            if (socket == null) {
                val socket = client.connectChat(scope)
                scope.launch {
                    socket.messageFlow.collect { message ->
                        state.set { it.copy(messages = stateNow.messages + message) }
                    }
                }
                this.socket = socket
            }
        }
        state.set { it.copy(isActive = value) }
    }

    fun setMessage(value: String) {
        state.set { it.copy(message = value) }
    }

    fun sendMessage() {
        val socket = socket ?: return
        socket.send(ChatMessage("user", stateNow.message, Clock.System.now()))
        state.set { it.copy(message = "") }
    }
}

data class ChatRoomState(
    val messages: List<ChatMessage> = emptyList(),
    val message: String = "",
    val isActive: Boolean = false,
)