@file:OptIn(ExperimentalSerializationApi::class)

package streetlight.web.io

import kampfire.api.Endpoint
import kampfire.api.GetByIdEndpoint
import kampfire.api.GetEndpoint
import kampfire.api.PathBuilder
import kampfire.api.PostEndpoint
import kampfire.api.QueryEndpoint
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import koala.external.FeedMessage
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array
import web.abort.AbortSignal
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

class FetchClient() {
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

    suspend inline fun <reified Returned, Endpoint : GetEndpoint<Returned>> getApi(
        endpoint: Endpoint,
        noinline block: (PathBuilder.(Endpoint) -> Unit)? = null,
    ): Outcome<Returned> = request(
        method = RequestMethod.GET,
        path = resolvePath(endpoint, block)
    ) { it.decodeBytes() }

    suspend inline fun <Id, reified Returned> getApi(
        endpoint: GetByIdEndpoint<Id, Returned>,
        id: Id,
    ): Outcome<Returned> = request(RequestMethod.GET, "${endpoint.path}/$id") { it.decodeBytes() }

    suspend inline fun <reified Sent, reified Returned> getApi(
        endpoint: QueryEndpoint<Sent, Returned>,
        query: String?
    ): Outcome<Returned> {
        val url = if (!query.isNullOrEmpty()) "${endpoint.path}?$query" else endpoint.path
        return request(RequestMethod.GET, url) { it.decodeBytes() }
    }

    suspend inline fun <reified Sent, reified Returned> postApi(
        endpoint: PostEndpoint<Sent, Returned>,
        body: Sent,
    ): Outcome<Returned> =
        request(RequestMethod.POST, endpoint.path, BodyInit(Json.encodeToString(body))) { it.decodeBytes() }

    suspend inline fun <reified Returned> postApi(
        endpoint: PostEndpoint<Unit, Returned>,
    ): Outcome<Returned> =
        request(RequestMethod.POST, endpoint.path, null) { it.decodeBytes() }

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

    fun connectSocket(
        endpoint: Endpoint<*, *>,
        vararg params: Pair<String, String>
    ) = connectSocket(endpoint.path, *params)

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

    fun connectSSE(
        endpoint: Endpoint<*, *>,
        vararg params: Pair<String, String>
    ) = connectSSE(endpoint.path, *params)

    fun connectSSE(
        path: String,
        vararg params: Pair<String, String>,
    ): EventSource {
        val fullPath = params.takeIf { it.isNotEmpty() }?.let {
            "$path?" + it.joinToString("&") { (k, v) -> "$k=$v" }
        } ?: path
        return EventSource(fullPath)
    }

    fun <E : Endpoint<*, *>> resolvePath(
        endpoint: E,
        block: (PathBuilder.(E) -> Unit)? = null
    ): String {
        val block = block ?: return endpoint.path
        val builder = PathBuilder(endpoint)
        builder.block(endpoint)
        return builder.build()
    }

    suspend fun <T> request(
        method: RequestMethod,
        path: String,
        body: BodyInit? = null,
        contentType: String = "application/json",
        acceptEncoding: EncodingType? = null,
        maxAttempts: Int = 3,
        timeout: Double? = null,
        handleResponse: suspend (Response) -> Outcome<T>
    ): Outcome<T> {
        val fetchRequest: suspend () -> Response = {
            fetch(path, RequestInit(
                method = method,
                headers = Headers().apply {
                    append("Content-Type", contentType)
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

        if (response.status == 401.toShort()) {
            return Problem("Not authorized.")
        }

        return handleResponse(response)
    }

    suspend fun uploadBlob(postUrl: String, blobUrl: Url): Outcome<Url> {
        val response = fetch(blobUrl.value)
        val blob = response.blob()
        return request(
            method = RequestMethod.POST,
            path = postUrl,
            body = blob,
            contentType = blob.type.ifEmpty { "application/octet-stream" }
        ) {
            it.decodeBytes()
        }
    }
}