@file:OptIn(ExperimentalSerializationApi::class)

package streetlight.web.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.WebSocket
import org.khronos.webgl.set
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType

class SocketClient<T>(
    private val scope: CoroutineScope,
    private val serializer: KSerializer<T>,
    private val serializersModule: SerializersModule? = null,
    private val provideSocket: suspend () -> WebSocket,
) {
    private val _itemFlow = MutableSharedFlow<T>(0, 8)
    val itemFlow: Flow<T> = _itemFlow

    private var socket: WebSocket? = null

    private val cbor = serializersModule?.let {
        Cbor { serializersModule = it }
    } ?: defaultCbor

    fun connect() {
        scope.launch {
            val socket = provideSocket().also { socket = it }
            socket.binaryType = BinaryType.ARRAYBUFFER
            socket.onmessage = { event ->
                scope.launch {
                    val data = event.data
                    if (data is ArrayBuffer) {
                        val bytes = Int8Array(data).unsafeCast<ByteArray>()
                        val message = decode(bytes) ?: return@launch
                        _itemFlow.emit(message)
                    }
                }
            }
        }
    }

    fun disconnect() {
        val socket = socket ?: error("socket not connected: ${this::class.simpleName}")
        socket.close()
        this.socket = null
    }

    fun send(message: T) {
        val socket = socket ?: error("socket not connected: ${this::class.simpleName}")
        val bytes = encode(message)
        socket.send(bytes.toInt8Array().buffer)
    }

    private fun ByteArray.toInt8Array(): Int8Array =
        Int8Array(size).also { arr ->
            for (i in indices) arr[i] = this[i]
        }

    private fun encode(message: T): ByteArray =
        cbor.encodeToByteArray(serializer, message)

    private fun decode(bytes: ByteArray): T? = try {
        cbor.decodeFromByteArray(serializer, bytes)
    } catch (e: Exception) {
        console.log(e.message)
        null
    }
}

val defaultCbor = Cbor.Default

inline fun <reified T> socketClientOf(
    scope: CoroutineScope,
    serializersModule: SerializersModule? = null,
    noinline provideSocket: suspend () -> WebSocket,
): SocketClient<T> = SocketClient(scope, serializer(), serializersModule, provideSocket)