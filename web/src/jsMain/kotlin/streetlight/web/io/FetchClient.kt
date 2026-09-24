@file:OptIn(ExperimentalSerializationApi::class)

package streetlight.web.io

import kampfire.api.Endpoint
import kampfire.api.GetByIdEndpoint
import kampfire.api.GetEndpoint
import kampfire.api.PathBuilder
import kampfire.api.PostEndpoint
import kampfire.api.QueryEndpoint
import kampfire.model.HttpProblem
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import koala.Image
import koala.external.FeedMessage
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array
import web.abort.AbortSignal
import web.blob.Blob
import web.blob.BlobPropertyBag
import web.form.FormData
import web.http.BodyInit
import web.http.GET
import web.http.Headers
import web.http.POST
import web.http.RequestCredentials
import web.http.RequestInit
import web.http.RequestMethod
import web.http.RequestMode
import web.http.Response
import web.http.blob
import web.http.fetch
import web.http.sameOrigin
import web.sockets.WebSocket
import web.sse.EventSource
import kotlin.let
import kotlin.time.Duration.Companion.milliseconds

/**
 * Performs every HTTP call to the Streetlight API, and opens its sockets and event streams.
 *
 * A call returns an [Outcome]: the response decoded from CBOR, or a [Problem] for a failed status or a request
 * that could not be made.
 */
class FetchClient() {
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

    /** Calls [endpoint] with the parameters [block] writes. */
    suspend inline fun <reified Returned, Endpoint : GetEndpoint<Returned>> getApi(
        endpoint: Endpoint,
        noinline block: (PathBuilder.(Endpoint) -> Unit)? = null,
    ): Outcome<Returned> = request(
        method = RequestMethod.GET,
        path = resolvePath(endpoint, block)
    ) { it.decodeBytes() }

    /** Calls [endpoint] with [id] as the last path segment. */
    suspend inline fun <Id, reified Returned, Endpoint : GetByIdEndpoint<Id, Returned>> getApi(
        endpoint: Endpoint,
        id: Id,
        noinline block: (PathBuilder.(Endpoint) -> Unit)? = null,
    ): Outcome<Returned> = request(
        method = RequestMethod.GET,
        path = resolvePath("${endpoint.path}/$id", endpoint, block)
    ) { it.decodeBytes() }

    /** Calls [endpoint] with [query] as its query string. */
    suspend inline fun <reified Sent, reified Returned> getApi(
        endpoint: QueryEndpoint<Sent, Returned>,
        query: String?
    ): Outcome<Returned> {
        val url = if (!query.isNullOrEmpty()) "${endpoint.path}?$query" else endpoint.path
        return request(RequestMethod.GET, url) { it.decodeBytes() }
    }

    /** Calls [endpoint] with [body] as JSON. */
    suspend inline fun <reified Sent, reified Returned> postApi(
        endpoint: PostEndpoint<Sent, Returned>,
        body: Sent,
    ): Outcome<Returned> =
        request(RequestMethod.POST, endpoint.path, BodyInit(Json.encodeToString(body))) { it.decodeBytes() }

    /** Calls [endpoint] with no body. */
    suspend inline fun <reified Returned> postApi(
        endpoint: PostEndpoint<Unit, Returned>,
    ): Outcome<Returned> =
        request(RequestMethod.POST, endpoint.path, null) { it.decodeBytes() }

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

    /** Opens a websocket to [endpoint] with [params]. */
    fun connectSocket(
        endpoint: Endpoint<*, *>,
        vararg params: Pair<String, String>
    ) = connectSocket(endpoint.path, *params)

    /** Opens a websocket to [path] on this host with [params], secure when the page is. */
    fun connectSocket(
        path: String,
        vararg params: Pair<String, String>
    ): WebSocket {
        val path = params.takeIf { it.isNotEmpty() }?.let {
            "$path?" + it.joinToString("&") { (k, v) -> "$k=$v" }
        } ?: path
        val protocol = if (window.location.protocol == "https:") "wss:" else "ws:"
        val host = window.location.host
        val socket = WebSocket("$protocol//$host$path")
        return socket
    }

    /** Opens an event stream from [endpoint] with [params]. */
    fun connectSSE(
        endpoint: Endpoint<*, *>,
        vararg params: Pair<String, String>
    ) = connectSSE(endpoint.path, *params)

    /** Opens an event stream from [path] with [params]. */
    fun connectSSE(
        path: String,
        vararg params: Pair<String, String>,
    ): EventSource {
        val fullPath = params.takeIf { it.isNotEmpty() }?.let {
            "$path?" + it.joinToString("&") { (k, v) -> "$k=$v" }
        } ?: path
        return EventSource(fullPath)
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

    /** Uploads the local file of [blobImage] to [postUrl] with its details, returning the stored image. */
    suspend fun uploadBlob(postUrl: String, blobImage: Image): Outcome<Image> {
        val response = fetch(blobImage.url.value)
        val blob = response.blob()

        val form = FormData()
        form.append(
            "metadata",
            Blob(
                arrayOf(Json.encodeToString(blobImage)),
                BlobPropertyBag(type = "application/json")
            )
        )
        form.append("file", blob, "upload")

        return request(
            method = RequestMethod.POST,
            path = postUrl,
            body = form,
            contentType = null
        ) {
            it.decodeBytes()
        }
    }
}