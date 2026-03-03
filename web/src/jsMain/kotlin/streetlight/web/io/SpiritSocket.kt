package streetlight.web.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.WebSocket

class SpiritSocket(
    private val api: ApiClient,
    private val scope: CoroutineScope
) {
    private val _deltaFlow = MutableSharedFlow<SpiritDelta>(
        replay = 20,
        extraBufferCapacity = 64
    )
    val deltaFlow: Flow<SpiritDelta> = _deltaFlow

    fun connect() {
        val socket = api.connectSpiritVision()
    }

    init {
        socket.onmessage = { event ->
            scope.launch {
                val data = event.data
                if (data is String) {
                    val delta = data.decode()
                    if (delta != null) {
                        _deltaFlow.emit(delta)
                    } else {
                        console.log("invalid spirit delta: $data")
                    }
                }
            }
        }
    }

    fun send(delta: SpiritDelta) {
        socket.send(delta.encode())
    }
}

private fun SpiritDelta.encode() = Json.encodeToString(this)

private fun String.decode(): SpiritDelta? {
    return try {
        Json.decodeFromString(this)
    } catch (e: Exception) {
        null
    }
}
