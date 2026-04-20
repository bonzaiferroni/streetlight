package streetlight.web.io

import kampfire.api.ApiNode
import kampfire.api.Endpoint
import kampfire.api.GetByIdEndpoint
import kampfire.api.GetByTableIdEndpoint
import kampfire.api.GetEndpoint
import kampfire.api.PathBuilder
import kampfire.api.PostEndpoint
import kampfire.api.QueryEndpoint
import kampfire.api.TableId
import kampfire.api.UserApi
import kampfire.model.ApiResponse
import kampfire.model.ApiResponseSerializer
import kampfire.model.Auth
import kampfire.model.Url
import kampfire.model.toUrl
import koala.external.FeedMessage
import koala.utils.jsonConfig
import koala.utils.prettyPrint
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.WebSocket
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import org.w3c.files.Blob
import streetlight.model.data.ProjectId
import streetlight.model.data.toProjectId
import streetlight.web.model.AuthClient
import streetlight.web.model.StarCred
import kotlin.js.Promise
import kotlin.js.json
import kotlin.let
import kotlin.text.ifEmpty

class FetchClient(
    private val cred: StarCred
) {
    val authClient = AuthClient(cred)

    suspend inline fun <reified Returned, Endpoint: GetEndpoint<Returned>> get(
        endpoint: Endpoint,
        acceptEncoding: EncodingType? = null,
        noinline block: (PathBuilder.(Endpoint) -> Unit)? = null
    ): Returned? =
        authRequest(
            method = "GET",
            path = resolvePath(endpoint, block),
            acceptEncoding = acceptEncoding
        ) { it.tryDecode(acceptEncoding) }

    suspend inline fun <Id, reified Returned> get(
        endpoint: GetByIdEndpoint<Id, Returned>,
        id: Id,
    ): Returned? =
        authRequest("GET", "${endpoint.path}/$id") { it.tryDecodeText() }

    suspend inline fun <Id: TableId<*>, reified Returned> get(
        endpoint: GetByTableIdEndpoint<Id, Returned>,
        id: Id
    ): Returned? =
        authRequest("GET", "${endpoint.path}/${id.value}") { it.tryDecodeText() }

    suspend inline fun <reified Sent, reified Returned> get(
        endpoint: QueryEndpoint<Sent, Returned>,
        query: String?
    ): Returned? {
        val url = if (!query.isNullOrEmpty()) "${endpoint.path}?$query" else endpoint.path
        return authRequest("GET", url) { it.tryDecodeText() }
    }

    suspend inline fun <reified Sent, reified Returned> post(
        endpoint: PostEndpoint<Sent, Returned>,
        body: Sent,
    ): Returned? = authRequest("POST", endpoint.path, Json.encodeToString(body)) { it.tryDecodeText() }

    suspend inline fun <reified Sent, reified Returned> postApi(
        endpoint: PostEndpoint<Sent, Returned>,
        body: Sent,
    ): ApiResponse<Returned>? = authRequest("POST", endpoint.path, Json.encodeToString(body)) { it.tryDecodeApiResponse() }

    suspend inline fun <reified Sent, reified Returned> postAndReadStatus(
        endpoint: PostEndpoint<Sent, Returned>,
        body: Sent,
    ): FetchResponse<Returned>? = authRequest("POST", endpoint.path, Json.encodeToString(body)) { it.tryDecodeWithStatus() }

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

    fun connectSocket(endpoint: Endpoint<*, *>) = connectSocket(endpoint.path)

    fun connectSocket(path: String): WebSocket {
        val protocol = if (window.location.protocol == "https:") "wss:" else "ws:"
        val host = window.location.host
        val socket = WebSocket("$protocol//$host$path")
        return socket
    }

    fun <E: Endpoint<*, *>> resolvePath(
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
        handleResponse: suspend (Response) -> T
    ): T? {
        val fetchWithJwt: suspend (String?) -> Response = { jwt ->
            val headers = json(
                "Content-Type" to contentType,
            )
            jwt?.let {
                headers["Authorization"] = "Bearer $jwt"
            }
            acceptEncoding?.let {
                headers["Accept"] = it.headerValue
            }
            val request = RequestInit(
                method = method,
                headers = headers,
                body = body,
                // cache = RequestCache.DEFAULT,
            )
            window.fetch(path, request).await()
        }

        var auth = cred.readAuth()
        var response = fetchWithJwt(auth?.jwt)

        // authenticate on 401
        if (response.status == 401.toShort()) {
            auth = authClient.authenticate() ?: return null

            response = fetchWithJwt(auth.jwt)
        }

        if (!response.ok) {
            console.log("$method to $path failed: ${response.status}")
            return null
        }

        return handleResponse(response)
    }

    suspend fun uploadBlob(endpoint: Endpoint<*, *>, blobUrl: Url) = uploadBlob(endpoint.path, blobUrl)

    suspend fun uploadBlob(postUrl: String, blobUrl: Url): Url? {
        val response = window.fetch(blobUrl.value).await()
        val blob: Blob = response.blob().await()
        return authRequest(
            method = "POST",
            path = postUrl,
            body = blob,
            contentType = blob.type.ifEmpty { "application/octet-stream" }
        ) {
            it.text().await().removeSurrounding("\"").toUrl()
        }
    }
}

suspend inline fun <reified Returned> Response.tryDecode(encoding: EncodingType?): Returned? {
    return when (encoding) {
        EncodingType.Cbor -> tryDecodeBytes()
        EncodingType.Json, null -> tryDecodeText()
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified Returned> Response.tryDecodeBytes(): Returned? {
    if (status.toInt() == 204 || !ok) return null
    val buffer = arrayBuffer().await()
    val bytes = Int8Array(buffer).unsafeCast<ByteArray>()
    return Cbor.decodeFromByteArray<Returned>(bytes)
}

suspend inline fun <reified Returned> Response.tryDecodeText(): Returned? {
    val text = text().await()

    return try {
        when (Returned::class) {
            String::class -> text as Returned

            Int::class -> text.toIntOrNull() as Returned?
            Long::class -> text.toLongOrNull() as Returned?

            Double::class -> text.toDoubleOrNull() as Returned?
            Float::class -> text.toFloatOrNull() as Returned?
            Boolean::class -> text.toBooleanStrictOrNull() as Returned?
            ProjectId::class -> text.toProjectId<Returned>()

            else -> jsonConfig.decodeFromString<Returned>(text)
        }
    } catch (e: Exception) {
        console.log("failed to parse response:\n${e}\n${url}\ndata: ${text.take(400)}")
        null
    }
}

suspend inline fun <reified T> Response.tryDecodeApiResponse(): ApiResponse<T>? {
    val text = text().await()
    return try {
        jsonConfig.decodeFromString(
            ApiResponseSerializer(serializer<T>()),
            text
        )
    } catch (e: Exception) {
        console.log("failed to parse response:\n${e}\n${url}\ndata: ${text.take(400)}")
        null
    }
}

suspend inline fun <reified Returned> Response.tryDecodeWithStatus(): FetchResponse<Returned> {
    val status = status.toInt()
    val payload: Returned? = if (status == 200) tryDecodeText() else null
    return FetchResponse(status, payload)
}

data class FetchResponse<T>(
    val status: Int,
    val payload: T?
) {
    val reason get() = when (status) {
        200 -> "Success"
        409 -> "Conflict"
        else -> "Unknown"
    }
}

enum class EncodingType(val headerValue: String) {
    Cbor("application/cbor"),
    Json("application/json")
}