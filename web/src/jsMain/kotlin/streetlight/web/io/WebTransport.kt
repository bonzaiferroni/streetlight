@file:OptIn(ExperimentalSerializationApi::class)

package streetlight.web.io

import io.ktor.http.HttpMethod
import js.typedarrays.toInt8Array
import kampfire.api.ApiCall
import kampfire.api.ApiSocket
import kampfire.api.ApiTransport
import kampfire.api.Endpoint
import kampfire.api.FilePart
import kampfire.api.FormPart
import kampfire.api.GetEndpoint
import kampfire.api.PathBuilder
import kampfire.api.TextPart
import kampfire.model.HttpProblem
import kampfire.model.Outcome
import kampfire.model.Problem
import koala.external.FeedMessage
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array
import web.abort.AbortSignal
import web.blob.Blob
import web.blob.BlobPropertyBag
import web.events.EventHandler
import web.events.EventType
import web.events.addEventListener
import web.form.FormData
import web.history.PageTransitionEvent
import web.http.BodyInit
import web.http.GET
import web.http.Headers
import web.http.POST
import web.http.RequestCredentials
import web.http.RequestInit
import web.http.RequestMethod
import web.http.RequestMode
import web.http.Response
import web.http.fetch
import web.http.sameOrigin
import web.sockets.WebSocket
import web.sse.EventSource
import kotlin.time.Duration.Companion.milliseconds

/**
 * The browser's [ApiTransport]: calls the Streetlight API with `fetch`, and opens its event streams and sockets.
 *
 * A call returns an [Outcome]: the response decoded from CBOR, or a [Problem] for a failed status or a request
 * that could not be made.
 */
class WebTransport : ApiTransport {
    override suspend fun <Sent, Returned> send(call: ApiCall<Sent, Returned>): Outcome<Returned> {
        val method = when (call.method) {
            HttpMethod.Get -> RequestMethod.GET
            HttpMethod.Post -> RequestMethod.POST
            else -> error("unsupported method: ${call.method}")
        }
        val form = call.form
        val sentSerializer = call.sentSerializer
        val sent = call.sent
        val body = when {
            form != null -> form.toFormData()
            sentSerializer != null && sent != null -> BodyInit(Json.encodeToString(sentSerializer, sent))
            else -> null
        }
        val contentType = if (form != null) null else "application/json"
        return request(method, call.path, body, contentType) { it.decodeBytes(call.returnedSerializer) }
    }

    override fun openEvents(path: String): Flow<String> = callbackFlow {
        val source = EventSource(path)
        source.onmessage = EventHandler { event -> trySend(event.data as String) }
        web.window.window.addEventListener(EventType<PageTransitionEvent>("pagehide"), { source.close() })
        awaitClose { source.close() }
    }

    override fun openSocket(path: String): ApiSocket {
        val protocol = if (window.location.protocol == "https:") "wss:" else "ws:"
        return BrowserSocket(WebSocket("$protocol//${window.location.host}$path"))
    }

    /** Calls [endpoint] with the parameters [block] writes, asking for [acceptEncoding]. */
    suspend inline fun <reified Returned, Endpoint : GetEndpoint<Returned>> get(
        endpoint: Endpoint,
        acceptEncoding: EncodingType? = null,
        noinline block: (PathBuilder.(Endpoint) -> Unit)? = null
    ): Outcome<Returned> =
        request(
            method = RequestMethod.GET,
            path = resolvePath(endpoint, block),
            acceptEncoding = acceptEncoding
        ) { it.decodeBytes() }

    /** Fetches [path] and decodes it as a GTFS feed of [feedType], or returns `null` when there is no content. */
    suspend inline fun <reified Returned> getProtobuf(
        path: String,
        feedType: ProtobufType
    ): FeedMessage<Returned>? {
        val response = window.fetch(path).await()
        if (response.status.toInt() == 204) {
            return null
        }
        val buffer = response.arrayBuffer().await()
        val array = Uint8Array(buffer)

        return feedType.decode(array)
    }

    /** The path of [endpoint] with the parameters [block] writes. */
    fun <E : Endpoint<*, *>> resolvePath(
        endpoint: E,
        block: (PathBuilder.(E) -> Unit)? = null
    ) = resolvePath(endpoint.path, endpoint, block)

    /** [path] with the parameters [block] writes for [endpoint]. */
    fun <E : Endpoint<*, *>> resolvePath(
        path: String,
        endpoint: E,
        block: (PathBuilder.(E) -> Unit)? = null
    ): String {
        val block = block ?: return path
        val builder = PathBuilder(path)
        builder.block(endpoint)
        return builder.build()
    }

    /**
     * Sends a request, retrying a failed fetch up to [maxAttempts] times, and reads the response with
     * [handleResponse].
     *
     * A 401, 409, 429 or 500 status becomes the matching [HttpProblem].
     */
    suspend fun <T> request(
        method: RequestMethod,
        path: String,
        body: BodyInit? = null,
        contentType: String? = "application/json",
        acceptEncoding: EncodingType? = null,
        maxAttempts: Int = 3,
        timeout: Double? = null,
        handleResponse: suspend (Response) -> Outcome<T>
    ): Outcome<T> {
        val fetchRequest: suspend () -> Response = {
            fetch(path, RequestInit(
                method = method,
                headers = Headers().apply {
                    contentType?.let {
                        append("Content-Type", contentType)
                    }
                    acceptEncoding?.let { append("Accept", it.headerValue) }
                },
                body = body,
                mode = RequestMode.sameOrigin,
                credentials = RequestCredentials.sameOrigin,
                signal = timeout?.let { AbortSignal.timeout(it) },
            ))
        }

        var response: Response? = null
        var attempt = 0
        while (response == null) {
            attempt++
            response = try {
                fetchRequest()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                console.log("retrying request")
                if (attempt >= maxAttempts) {
                    console.log("Unable to fetch request: ${e.message}")
                    return Problem("Unable to make a request.")
                }
                delay((500L * attempt).milliseconds)
                null
            }
        }

        when (response.status.toInt()) {
            401 -> return HttpProblem.NotAuthorized
            429 -> return HttpProblem.TooManyRequests
            409 -> return HttpProblem.Conflict
            500 -> return HttpProblem.InternalServerError
        }

        return handleResponse(response)
    }
}

/** An [ApiSocket] over a browser [WebSocket], holding sends until it opens. */
private class BrowserSocket(private val socket: WebSocket) : ApiSocket {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val messages: Flow<String> = _messages

    private val pending = mutableListOf<String>()
    private var isOpen = false

    init {
        socket.onmessage = EventHandler { event ->
            val data = event.data
            if (data is String) _messages.tryEmit(data)
        }
        socket.onopen = EventHandler {
            isOpen = true
            pending.forEach { socket.send(it) }
            pending.clear()
        }
    }

    override fun send(text: String) {
        if (isOpen) socket.send(text) else pending.add(text)
    }

    override fun close() = socket.close()
}

/** The form as browser [FormData]. */
private fun List<FormPart>.toFormData() = FormData().also { data ->
    forEach { part ->
        when (part) {
            is TextPart -> when (val type = part.contentType) {
                null -> data.append(part.name, part.value)
                else -> data.append(part.name, Blob(arrayOf(part.value), BlobPropertyBag(type = type)))
            }
            is FilePart -> data.append(part.name, Blob(arrayOf(part.bytes.toInt8Array())), part.fileName)
        }
    }
}
