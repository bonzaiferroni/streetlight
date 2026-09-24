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
import web.buffer.BinaryType
import web.buffer.arraybuffer
import web.events.EventHandler
import web.sockets.WebSocket
import js.buffer.ArrayBuffer
import js.typedarrays.Int8Array
import js.typedarrays.toByteArray
import js.typedarrays.toInt8Array

/** A websocket carrying CBOR: [messageFlow] emits each message received, and [send] writes a request. */
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

    /** Opens the socket from [provideSocket]. */
    fun connect() {
        scope.launch {
            val socket = provideSocket().also { socket = it }
            socket.binaryType = BinaryType.arraybuffer
            socket.onmessage = EventHandler { event ->
                scope.launch {
                    val data = event.data
                    if (data is ArrayBuffer) {
                        val message = decode(Int8Array(data).toByteArray()) ?: return@launch
                        _itemFlow.emit(message)
                    }
                }
            }
        }
    }

    /** Closes the socket. Throws when it is not connected. */
    fun disconnect() {
        val socket = socket ?: error("socket not connected: ${this::class.simpleName}")
        socket.close()
        this.socket = null
    }

    /** Sends [request]. Throws when the socket is not connected or has no request serializer. */
    fun send(request: Request) {
        val socket = socket ?: error("socket not connected: ${this::class.simpleName}")
        socket.send(encode(request).toInt8Array())
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

/** The CBOR format of the app's sockets and responses. */
val defaultCbor = Cbor.Default

/** A [SocketClient] that only receives. */
inline fun <reified Message> socketClientOf(
    scope: CoroutineScope,
    noinline provideSocket: suspend () -> WebSocket,
): SocketClient<Message, Unit> = SocketClient(scope, serializer(), null, provideSocket)

/** A [SocketClient] that receives and sends. */
inline fun <reified Message, reified Request> socketRequestClientOf(
    scope: CoroutineScope,
    noinline provideSocket: suspend () -> WebSocket,
): SocketClient<Message, Request> = SocketClient(scope, serializer(), serializer(), provideSocket)