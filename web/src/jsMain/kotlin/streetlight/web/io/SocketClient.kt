@file:OptIn(ExperimentalSerializationApi::class)

package streetlight.web.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.serializer
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.WebSocket
import org.khronos.webgl.set
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType

class SocketClient<Message, Request>(
    private val scope: CoroutineScope,
    private val messageSerializer: KSerializer<Message>,
    private val requestSerializer: KSerializer<Request>? = null,
    private val provideSocket: suspend () -> WebSocket,
) {
    private val _itemFlow = MutableSharedFlow<Message>(0, 8)
    val messageFlow: Flow<Message> = _itemFlow

    private var socket: WebSocket? = null

    private val cbor = defaultCbor

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

    fun send(request: Request) {
        val socket = socket ?: error("socket not connected: ${this::class.simpleName}")
        val bytes = encode(request)
        socket.send(bytes.toInt8Array().buffer)
    }

    private fun ByteArray.toInt8Array(): Int8Array =
        Int8Array(size).also { arr ->
            for (i in indices) arr[i] = this[i]
        }

    private fun encode(request: Request): ByteArray = requestSerializer?.let {
        cbor.encodeToByteArray(it, request)
    } ?: error("request serializer not found")


    private fun decode(bytes: ByteArray): Message? = try {
        cbor.decodeFromByteArray(messageSerializer, bytes)
    } catch (e: Exception) {
        console.log(e.message)
        null
    }
}

val defaultCbor = Cbor.Default

inline fun <reified Message> socketClientOf(
    scope: CoroutineScope,
    noinline provideSocket: suspend () -> WebSocket,
): SocketClient<Message, Unit> = SocketClient(scope, serializer(), null, provideSocket)

inline fun <reified Message, reified Request> socketRequestClientOf(
    scope: CoroutineScope,
    noinline provideSocket: suspend () -> WebSocket,
): SocketClient<Message, Request> = SocketClient(scope, serializer(), serializer(), provideSocket)