@file:OptIn(ExperimentalSerializationApi::class)

package streetlight.compose.io

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kampfire.api.ApiCall
import kampfire.api.ApiSocket
import kampfire.api.ApiTransport
import kampfire.api.FilePart
import kampfire.api.FormPart
import kampfire.api.TextPart
import kampfire.model.HttpProblem
import kampfire.model.Outcome
import kampfire.model.OutcomeSerializer
import kampfire.model.Problem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

/**
 * The app's [ApiTransport]: calls the Streetlight API at [baseUrl] with Ktor, keeping the session's cookies.
 *
 * A call returns an [Outcome]: the response decoded from CBOR, or a [Problem] for a failed status or a request
 * that could not be made.
 */
class AppTransport(private val baseUrl: String) : ApiTransport {
    private val client = HttpClient(CIO) {
        install(HttpCookies)
        install(SSE)
        install(WebSockets)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun <Sent, Returned> send(call: ApiCall<Sent, Returned>): Outcome<Returned> {
        val form = call.form
        val sentSerializer = call.sentSerializer
        val sent = call.sent
        val response = requestWithRetry {
            client.request(baseUrl + call.path) {
                method = call.method
                when {
                    form != null -> setBody(form.toFormContent())
                    sentSerializer != null && sent != null ->
                        setBody(TextContent(Json.encodeToString(sentSerializer, sent), ContentType.Application.Json))
                }
            }
        } ?: return Problem("Unable to make a request.")

        when (response.status.value) {
            401 -> return HttpProblem.NotAuthorized
            429 -> return HttpProblem.TooManyRequests
            409 -> return HttpProblem.Conflict
            500 -> return HttpProblem.InternalServerError
            200 -> Unit
            else -> return Problem("Unhandled error: ${response.status.value}")
        }

        return try {
            Cbor.decodeFromByteArray(OutcomeSerializer(call.returnedSerializer), response.bodyAsBytes())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Problem("Unable to parse response.")
        }
    }

    override fun openEvents(path: String): Flow<String> = channelFlow {
        client.sse(baseUrl + path) {
            incoming.collect { event -> event.data?.let { send(it) } }
        }
    }

    override fun openSocket(path: String): ApiSocket = AppSocket(baseUrl.replaceFirst("http", "ws") + path)

    /** Sends a request, retrying a failed attempt up to [maxAttempts] times, or returns `null` when none succeeds. */
    private suspend fun requestWithRetry(maxAttempts: Int = 3, request: suspend () -> HttpResponse): HttpResponse? {
        repeat(maxAttempts) { attempt ->
            try {
                return request()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                delay((500L * (attempt + 1)).milliseconds)
            }
        }
        return null
    }

    /** An [ApiSocket] over a Ktor websocket session, holding sends until it opens. */
    private inner class AppSocket(url: String) : ApiSocket {
        private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 64)
        override val messages: Flow<String> = _messages

        private val outgoing = Channel<String>(Channel.UNLIMITED)

        private val job = scope.launch {
            client.webSocket(url) {
                launch {
                    for (text in this@AppSocket.outgoing) send(Frame.Text(text))
                }
                for (frame in incoming) {
                    if (frame is Frame.Text) _messages.emit(frame.readText())
                }
            }
        }

        override fun send(text: String) {
            outgoing.trySend(text)
        }

        override fun close() {
            job.cancel()
            outgoing.close()
        }
    }
}

/** The form as Ktor multipart content. */
private fun List<FormPart>.toFormContent() = MultiPartFormDataContent(formData {
    forEach { part ->
        when (part) {
            is TextPart -> when (val type = part.contentType) {
                null -> append(part.name, part.value)
                else -> append(part.name, part.value, Headers.build { append(HttpHeaders.ContentType, type) })
            }
            is FilePart -> append(part.name, part.bytes, Headers.build {
                append(HttpHeaders.ContentDisposition, "filename=\"${part.fileName}\"")
            })
        }
    }
})
