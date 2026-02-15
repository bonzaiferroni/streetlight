package streetlight.agent

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kabinet.console.globalConsole
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import streetlight.model.Api

private val console = globalConsole.getHandle(ChatSocket::class)

class ChatSocket {
    private val _messageFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 64
    )
    val messageFlow: Flow<String> = _messageFlow
    private val sendFlow = MutableSharedFlow<String>()

    suspend fun connect() {
        console.log("connecting")
        client.use { client ->
            client.webSocket(host = "localhost", port = 8080, path = Api.Chat.path) {
                val incomingJob = launch {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> _messageFlow.emit(frame.readText())
                            else -> Unit
                        }
                    }
                }

                val outgoingJob = launch {
                    sendFlow.collect { message ->
                        send(Frame.Text(message))
                    }
                }

                incomingJob.join()
                outgoingJob.cancelAndJoin()
            }
        }
    }

    suspend fun send(message: String) {
        sendFlow.emit(message)
    }
}

val client = HttpClient(CIO) {
    install(WebSockets)
}