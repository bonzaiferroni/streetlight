package streetlight.server.routes

import io.ktor.server.routing.Routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kabinet.console.globalConsole
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import java.util.Collections

private val console = globalConsole.getHandle(Routing::serveChat.name)

private val history = Collections.synchronizedList(mutableListOf<String>())

fun Routing.serveChat(app: ServerProvider = RuntimeProvider) {
    val clients = Collections.synchronizedSet<DefaultWebSocketServerSession>(
        LinkedHashSet()
    )

    webSocket(Api.Chat.path) {
        clients += this
        try {
            val currentHistory = synchronized(history) { history.toList() }
            currentHistory.forEach { message ->
                send(message)
            }
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    synchronized(history) {
                        history.add(text)
                        while (history.size > 20) {
                            history.removeAt(0)
                        }
                    }
                    clients.forEach { client ->
                        client.send(text)
                    }
                }
            }
        } finally {
            clients -= this
        }
    }
}