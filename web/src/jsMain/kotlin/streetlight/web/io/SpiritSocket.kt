package streetlight.web.io

import kampfire.model.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import streetlight.model.data.Spirit
import streetlight.model.data.SpiritFrame
import web.events.EventHandler
import web.sockets.WebSocket

class SpiritSocket(
    private val api: ApiClient,
    private val scope: CoroutineScope
) {
    private val _spiritFlow = MutableSharedFlow<SpiritFrame>(1)
    val spiritFlow: Flow<SpiritFrame> = _spiritFlow
    
    private var socket: WebSocket? = null
    private var spirit: Spirit? = null
    
    fun connect(spirit: Spirit) {
        this.spirit = spirit
        val socket = api.connectSpiritVision().also { this.socket = it }
        socket.onmessage = EventHandler { event ->
            scope.launch {
                val data = event.data
                if (data is String) {
                    val frame = data.decode() ?: return@launch
                    _spiritFlow.emit(frame)
                }
            }
        }
        socket.onopen = EventHandler {
            socket.send(SpiritFrame.Initial(spirit).encode())
        }
    }

    fun disconnect() {
        socket?.close()
        socket = null
    }
    
    fun updatePosition(point: GeoPoint) {
        spirit = spirit?.copy(position = point)
        val spiritId = spirit?.spiritId ?: return
        send(SpiritFrame.Position(id = spiritId, pos = point))
    }

    private fun send(delta: SpiritFrame) {
        val socket = socket ?: return
        socket.send(delta.encode())
    }
}

private fun SpiritFrame.encode() = Json.encodeToString(this)

private fun String.decode(): SpiritFrame? {
    return try {
        Json.decodeFromString<SpiritFrame>(this)
    } catch (e: Exception) {
        console.log(e.message)
        null
    }
}