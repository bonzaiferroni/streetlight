package streetlight.web.io

import kampfire.api.Endpoint
import kampfire.api.GetByIdEndpoint
import kampfire.api.GetByTableIdEndpoint
import kampfire.api.GetEndpoint
import kampfire.api.PathBuilder
import kampfire.api.PostEndpoint
import kampfire.api.QueryEndpoint
import kampfire.api.TableId
import kampfire.api.UserApi
import kampfire.model.Auth
import koala.external.FeedMessage
import koala.utils.jsonConfig
import koala.utils.prettyPrint
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array
import org.w3c.dom.WebSocket
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import org.w3c.files.Blob
import streetlight.web.model.UserCred
import kotlin.js.json
import kotlin.let
import kotlin.text.ifEmpty

class FetchClient(
    private val cred: UserCred?
) {
    suspend inline fun <reified Returned, Endpoint: GetEndpoint<Returned>> get(
        endpoint: Endpoint,
        noinline block: (PathBuilder.(Endpoint) -> Unit)? = null
    ): Returned? =
        authRequest("GET", resolvePath(endpoint, block) ) { it.tryDecodeText() }

    suspend inline fun <reified Returned> get(
        endpoint: GetByIdEndpoint<String, Returned>,
        id: String,
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

    suspend inline fun <reified Sent, reified Returned> postAndReadStatus(
        endpoint: PostEndpoint<Sent, Returned>,
        body: Sent,
    ): FetchResponse<Returned>? = authRequest("POST", endpoint.path, Json.encodeToString(body)) { it.tryDecodeWithStatus() }

    suspend inline fun <reified Returned> getProtobuf(
        endpoint: GetEndpoint<Unit>,
        feedType: ProtobufType
    ): FeedMessage<Returned> {
        val response = window.fetch(endpoint.path).await()
            .arrayBuffer().await()
        val buffer = Uint8Array(response)

        return feedType.decode(buffer)
    }

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
        handleResponse: suspend (Response) -> T
    ): T? {
        val fetchWithJwt: suspend (String?) -> Response = { jwt ->
            val headers = jwt?.let {
                json(
                    "Content-Type" to contentType,
                    "Authorization" to "Bearer $jwt"
                )
            } ?: json(
                "Content-Type" to contentType,
            )
            val request = RequestInit(
                method = method,
                headers = headers,
                body = body
            )
            window.fetch(path, request).await()
        }

        var auth = cred?.readAuth()
        var response = fetchWithJwt(auth?.jwt)

        if (response.status == 401.toShort()) {
            val loginRequest = cred?.getLoginRequest()
            if (loginRequest == null) {
                console.log("credentials not found")
                return null
            }
            console.log("authorizing")
            val loginResponse = window.fetch(
                UserApi.Login.path,
                RequestInit(
                    method = "POST",
                    headers = json(
                        "Content-Type" to "application/json",
                    ),
                    body = Json.encodeToString(loginRequest),
                )
            ).await()

            if (!loginResponse.ok) {
                console.log("Login failed")
                return null
            }

            val loginText = loginResponse.text().await()
            auth = Json.decodeFromString<Auth>(loginText)
            cred.writeAuth(auth)

            response = fetchWithJwt(auth.jwt)
        }

        if (!response.ok) {
            console.log("$method to $path failed: ${response.status}")
            return null
        }

        return handleResponse(response)
    }

    suspend fun uploadBlob(postUrl: String, blobUrl: String): String? {
        val response = window.fetch(blobUrl).await()
        val blob: Blob = response.blob().await()
        return authRequest(
            method = "POST",
            path = postUrl,
            body = blob,
            contentType = blob.type.ifEmpty { "application/octet-stream" }
        ) {
            it.text().await()
        }
    }
}

suspend inline fun <reified Returned> Response.tryDecodeText(debug: Boolean = false): Returned? {
    val text = text().await()

    if (debug) {
        console.log(text)
    }

    return try {
        when (Returned::class) {
            String::class -> text as Returned

            Int::class -> text.toIntOrNull() as Returned?
            Long::class -> text.toLongOrNull() as Returned?

            Double::class -> text.toDoubleOrNull() as Returned?
            Float::class -> text.toFloatOrNull() as Returned?
            Boolean::class -> text.toBooleanStrictOrNull() as Returned?

            else -> jsonConfig.decodeFromString<Returned>(text)
        }
    } catch (e: Exception) {
        console.log("failed to parse response:\n${e}\ndata: ${text.take(400)}")
        null
    }
}

suspend inline fun <reified Returned> Response.tryDecodeWithStatus(debug: Boolean = false): FetchResponse<Returned> {
    val status = status.toInt()
    val payload: Returned? = if (status == 200) tryDecodeText(debug) else null
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