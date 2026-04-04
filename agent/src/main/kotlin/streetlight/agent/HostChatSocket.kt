package streetlight.agent

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kabinet.console.globalConsole
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import streetlight.model.Api
import streetlight.model.data.ChatMessage

private val console = globalConsole.getHandle(HostChatSocket::class)

class HostChatSocket {
    private val _messageFlow = MutableSharedFlow<ChatMessage>(
        replay = 20,
        extraBufferCapacity = 64
    )
    val messageFlow: Flow<ChatMessage> = _messageFlow
    private val sendFlow = MutableSharedFlow<ChatMessage>(
        extraBufferCapacity = 64
    )

    suspend fun connect() {
        console.log("connecting")
        client.webSocket(host = "localhost", port = 8080, path = Api.Chat.path) {
            val incomingJob = launch {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            frame.readText().decode()?.let {
                                _messageFlow.emit(it)
                            }
                        }
                        else -> Unit
                    }
                }
            }

            val outgoingJob = launch {
                sendFlow.collect { message ->
                    send(Frame.Text(message.encode()))
                }
            }

            incomingJob.join()
            outgoingJob.cancelAndJoin()
        }
    }

    suspend fun send(message: ChatMessage) {
        sendFlow.emit(message)
    }
}

private fun ChatMessage.encode() = Json.encodeToString(this)

private fun String.decode(): ChatMessage? = try {
    Json.decodeFromString(this)
} catch (e: Exception) {
    null
}

val client = HttpClient(CIO) {
    install(WebSockets) {
    }
}