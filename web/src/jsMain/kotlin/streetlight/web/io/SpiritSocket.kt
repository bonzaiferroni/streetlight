package streetlight.web.io

import kampfire.api.MessageSocket
import kampfire.model.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.Spirit
import streetlight.model.data.SpiritFrame
import streetlight.model.io.ApiClient

/** The spirit socket: announces this visitor's spirit and position, and emits the frames of the others. */
class SpiritSocket(
    private val api: ApiClient,
    private val scope: CoroutineScope
) {
    private val _spiritFlow = MutableSharedFlow<SpiritFrame>(1)
    val spiritFlow: Flow<SpiritFrame> = _spiritFlow
    
    private var socket: MessageSocket<SpiritFrame, SpiritFrame>? = null
    private var spirit: Spirit? = null
    
    /** Opens the socket and announces [spirit]. */
    fun connect(spirit: Spirit) {
        this.spirit = spirit
        val socket = api.post.connectSpiritVision().also { this.socket = it }
        scope.launch {
            socket.messages.collect { _spiritFlow.emit(it) }
        }
        socket.send(SpiritFrame.Initial(spirit))
    }

    fun disconnect() {
        socket?.close()
        socket = null
    }
    
    /** Moves this visitor's spirit to [point]. */
    fun updatePosition(point: GeoPoint) {
        spirit = spirit?.copy(position = point)
        val spiritId = spirit?.spiritId ?: return
        send(SpiritFrame.Position(id = spiritId, pos = point))
    }

    private fun send(delta: SpiritFrame) {
        val socket = socket ?: return
        socket.send(delta)
    }
}
