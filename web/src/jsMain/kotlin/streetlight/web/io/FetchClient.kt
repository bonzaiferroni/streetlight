@file:OptIn(ExperimentalSerializationApi::class)

package streetlight.web.io

import kampfire.api.Endpoint
import kampfire.api.GetByIdEndpoint
import kampfire.api.GetByTableIdEndpoint
import kampfire.api.GetEndpoint
import kampfire.api.PathBuilder
import kampfire.api.PostEndpoint
import kampfire.api.QueryEndpoint
import kampfire.api.TableId
import kampfire.model.Outcome
import kampfire.model.Url
import koala.external.FeedMessage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array
import org.w3c.dom.EventSource
import org.w3c.dom.WebSocket
import org.w3c.fetch.DEFAULT
import org.w3c.fetch.FOLLOW
import org.w3c.fetch.RequestCache
import org.w3c.fetch.RequestCredentials
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import org.w3c.fetch.RequestRedirect
import org.w3c.fetch.Response as FetchResponse
import org.w3c.fetch.SAME_ORIGIN
import org.w3c.files.Blob
import streetlight.web.model.AuthClient
import streetlight.web.model.CredentialStore
import kotlin.js.json
import kotlin.let
import kotlin.text.ifEmpty

class FetchClient(
    private val cred: CredentialStore
) {
    val authClient = AuthClient(cred)

    suspend inline fun <reified Returned, Endpoint : GetEndpoint<Returned>> get(
        endpoint: Endpoint,
        acceptEncoding: EncodingType? = null,
        noinline block: (PathBuilder.(Endpoint) -> Unit)? = null
    ): Returned? =
        authRequest(
            method = "GET",
            path = resolvePath(endpoint, block),
            acceptEncoding = acceptEncoding
        ) { it.tryDecode(acceptEncoding) }

    @Deprecated("use getApi")
    suspend inline fun <Id, reified Returned> get(
        endpoint: GetByIdEndpoint<Id, Returned>,
        id: Id,
    ): Returned? = authRequest("GET", "${endpoint.path}/$id") { it.tryDecodeText() }

    @Deprecated("use getApi")
    suspend inline fun <Id : TableId<*>, reified Returned> get(
        endpoint: GetByTableIdEndpoint<Id, Returned>,
        id: Id
    ): Returned? = authRequest("GET", "${endpoint.path}/${id.value}") { it.tryDecodeText() }

    @Deprecated("use getApi")
    suspend inline fun <reified Sent, reified Returned> post(
        endpoint: PostEndpoint<Sent, Returned>,
        body: Sent,
    ): Returned? = authRequest("POST", endpoint.path, Json.encodeToString(body)) { it.tryDecodeText() }

    suspend inline fun <reified Returned, Endpoint : GetEndpoint<Returned>> getApi(
        endpoint: Endpoint,
        noinline block: (PathBuilder.(Endpoint) -> Unit)? = null,
    ): Outcome<Returned>? = authRequest(
        method = "GET",
        path = resolvePath(endpoint, block)
    ) { it.tryDecodeBytesResponse() }

    suspend inline fun <Id, reified Returned> getApi(
        endpoint: GetByIdEndpoint<Id, Returned>,
        id: Id,
    ): Outcome<Returned>? = authRequest("GET", "${endpoint.path}/$id") { it.tryDecodeBytesResponse() }

    suspend inline fun <reified Sent, reified Returned> getApi(
        endpoint: QueryEndpoint<Sent, Returned>,
        query: String?
    ): Outcome<Returned>? {
        val url = if (!query.isNullOrEmpty()) "${endpoint.path}?$query" else endpoint.path
        return authRequest("GET", url) { it.tryDecodeBytesResponse() }
    }

    suspend inline fun <reified Sent, reified Returned> postApi(
        endpoint: PostEndpoint<Sent, Returned>,
        body: Sent,
    ): Outcome<Returned>? =
        authRequest("POST", endpoint.path, Json.encodeToString(body)) { it.tryDecodeBytesResponse() }

    @Deprecated("intended to be a general purpose request function but it is not working")
    suspend fun request(
        endpoint: Endpoint<*, *>,
        encodingType: EncodingType,
        acceptEncoding: EncodingType,
    ): FetchResponse {
        val headers = json(
            "Content-Type" to encodingType.headerValue,
            "Accept" to acceptEncoding.headerValue
        )
        return window.fetch(
            endpoint.path,
            RequestInit(
                method = endpoint.method?.value ?: error("method not found"),
                headers = headers,
                credentials = RequestCredentials.SAME_ORIGIN,
            )
        ).await()
    }

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

    suspend fun <T> authRequest(
        method: String,
        path: String,
        body: dynamic? = null,
        contentType: String = "application/json",
        acceptEncoding: EncodingType? = null,
        handleResponse: suspend (FetchResponse) -> T
    ): T? {
        val fetchWithJwt: suspend () -> FetchResponse = {
            val headers = json(
                "Content-Type" to contentType,
            )
            acceptEncoding?.let {
                headers["Accept"] = it.headerValue
            }
            val request = RequestInit(
                method = method,
                headers = headers,
                body = body,
                cache = RequestCache.DEFAULT,
                mode = RequestMode.SAME_ORIGIN,
                redirect = RequestRedirect.FOLLOW,
                credentials = RequestCredentials.SAME_ORIGIN,
                referrerPolicy = "".asDynamic(),
                integrity = "",
            )
            window.fetch(path, request).await()
        }

        var response = fetchWithJwt()

        // authenticate on 401
        if (response.status == 401.toShort()) {
            if (!authClient.authenticate()) return null
            response = fetchWithJwt()
        }

        return handleResponse(response)
    }

    suspend fun uploadBlob(postUrl: String, blobUrl: Url): Outcome<Url>? {
        val response = window.fetch(blobUrl.value).await()
        val blob: Blob = response.blob().await()
        return authRequest(
            method = "POST",
            path = postUrl,
            body = blob,
            contentType = blob.type.ifEmpty { "application/octet-stream" }
        ) {
            it.tryDecodeBytesResponse()
        }
    }
}