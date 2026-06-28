package streetlight.agent

import kabinet.console.globalConsole
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Clock

// private val console = globalConsole.getHandle(ChatAgentConnection::class)

class ChatAgentConnection(
    apiKey: String,
) {
    private val socket = HostChatSocket()
    private val agent = ChatAgent(apiKey) {
        socket.send(it)
    }

    suspend fun connect() = coroutineScope {
        launch {
            socket.connect()
        }

        val connectedAt = Clock.System.now()

        socket.messageFlow.collect { message ->
            if (message.source == agent.identifier) return@collect
            // console.log("received: $message")
            if (message.sentAt > connectedAt)
                agent.takeInput(message)
        }
    }
}