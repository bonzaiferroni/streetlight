package kampfire.api

import io.ktor.http.parseQueryString
import kampfire.model.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/** Calls [endpoint] with the parameters [block] writes. */
suspend inline fun <reified Returned, E : GetEndpoint<Returned>> ApiTransport.getApi(
    endpoint: E,
    noinline block: (PathBuilder.(E) -> Unit)? = null,
): Outcome<Returned> = send(ApiCall(endpoint, serializer(), parameters = parametersOf(endpoint, block)))

/** Calls [endpoint] with [id] as the last path segment. */
suspend inline fun <Id, reified Returned, E : GetByIdEndpoint<Id, Returned>> ApiTransport.getApi(
    endpoint: E,
    id: Id,
    noinline block: (PathBuilder.(E) -> Unit)? = null,
): Outcome<Returned> = send(ApiCall(endpoint, serializer(), sent = id, parameters = parametersOf(endpoint, block)))

/** Calls [endpoint] with [query] as its query string. */
suspend inline fun <Sent, reified Returned> ApiTransport.getApi(
    endpoint: QueryEndpoint<Sent, Returned>,
    query: String?,
): Outcome<Returned> = send(ApiCall(endpoint, serializer(), parameters = parseQueryString(query ?: "")))

/** Calls [endpoint] with [body] as JSON. */
suspend inline fun <reified Sent, reified Returned> ApiTransport.postApi(
    endpoint: PostEndpoint<Sent, Returned>,
    body: Sent,
): Outcome<Returned> = send(ApiCall(endpoint, serializer(), sent = body, sentSerializer = serializer()))

/** Calls [endpoint] with no body. */
suspend inline fun <reified Returned> ApiTransport.postApi(
    endpoint: PostEndpoint<Unit, Returned>,
): Outcome<Returned> = send(ApiCall(endpoint, serializer()))

/** Calls [endpoint] with [parts] as a multipart form. */
suspend inline fun <Sent, reified Returned> ApiTransport.postForm(
    endpoint: PostEndpoint<Sent, Returned>,
    parts: List<FormPart>,
): Outcome<Returned> = send(ApiCall(endpoint, serializer(), form = parts))

/** Each JSON message of the event stream at [endpoint] with [params], open while collected. Unreadable messages are skipped. */
inline fun <reified Message> ApiTransport.openEvents(
    endpoint: Endpoint<*, *>,
    vararg params: Pair<String, String>,
): Flow<Message> {
    val serializer = serializer<Message>()
    return openEvents(pathOf(endpoint, params)).mapNotNull { decodeOrNull(serializer, it) }
}

/** A socket to [endpoint] with [params], carrying JSON. */
inline fun <reified Received, reified Sent> ApiTransport.openSocket(
    endpoint: Endpoint<*, *>,
    vararg params: Pair<String, String>,
): MessageSocket<Received, Sent> = MessageSocket(openSocket(pathOf(endpoint, params)), serializer(), serializer())

/** An [ApiSocket] carrying [Received] and [Sent] as JSON. Unreadable messages are skipped. */
class MessageSocket<Received, Sent>(
    private val socket: ApiSocket,
    private val receivedSerializer: KSerializer<Received>,
    private val sentSerializer: KSerializer<Sent>,
) {
    val messages: Flow<Received> = socket.messages.mapNotNull { decodeOrNull(receivedSerializer, it) }

    fun send(message: Sent) = socket.send(Json.encodeToString(sentSerializer, message))

    fun close() = socket.close()
}

/** The query parameters [block] writes for [endpoint]. */
fun <E : Endpoint<*, *>> parametersOf(endpoint: E, block: (PathBuilder.(E) -> Unit)?) =
    PathBuilder(endpoint.path).apply { block?.invoke(this, endpoint) }.buildParameters()

/** The path of [endpoint] with [params] as its query string. */
fun pathOf(endpoint: Endpoint<*, *>, params: Array<out Pair<String, String>>) = when {
    params.isEmpty() -> endpoint.path
    else -> endpoint.path + "?" + params.joinToString("&") { (key, value) -> "$key=$value" }
}

/** [text] decoded with [serializer], or `null` when it does not decode. */
fun <T> decodeOrNull(serializer: KSerializer<T>, text: String): T? =
    try { socketJson.decodeFromString(serializer, text) } catch (e: Exception) { null }

/** The JSON format event and socket messages are read with. */
val socketJson = Json { ignoreUnknownKeys = true; explicitNulls = false }
