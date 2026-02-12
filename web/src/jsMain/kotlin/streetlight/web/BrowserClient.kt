package streetlight.web

import kampfire.api.GetByTableIdEndpoint
import kampfire.api.GetEndpoint
import kampfire.api.PostEndpoint
import kampfire.api.QueryEndpoint
import kampfire.api.TableId
import kampfire.api.UserApi
import kampfire.model.Auth
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import org.w3c.files.Blob
import kotlin.js.json
import kotlin.let
import kotlin.text.ifEmpty

suspend inline fun <reified Returned> AppContext.get(endpoint: GetEndpoint<Returned>): Returned? =
    authRequest("GET", endpoint.path) { request ->
        request.text().await().let { Json.decodeFromString(it) }
    }

suspend inline fun <Id: TableId<*>, reified Returned> AppContext.get(
    endpoint: GetByTableIdEndpoint<Id, Returned>,
    id: Id
): Returned? =
    authRequest("GET", "${endpoint.path}/${id.value}") { request ->
        request.text().await().let { Json.decodeFromString(it)}
    }

suspend inline fun <reified Sent, reified Returned> AppContext.get(
    endpoint: QueryEndpoint<Sent, Returned>,
    query: String?
): Returned? {
    val url = if (!query.isNullOrEmpty()) "${endpoint.path}?$query" else endpoint.path
    return authRequest("GET", url) { request ->
        request.text().await().let { Json.decodeFromString(it) }
    }
}

suspend inline fun <reified Sent, reified Returned> AppContext.post(
    endpoint: PostEndpoint<Sent, Returned>,
    body: Sent,
): Returned? = authRequest("POST", endpoint.path, Json.encodeToString(body)) { request ->
    request.text().await().let { Json.decodeFromString(it) }
}

suspend inline fun <reified Returned> getProtobuf(
    endpoint: GetEndpoint<Unit>,
    feedType: ProtobufType
): FeedMessage<Returned> {
    val response = window.fetch(endpoint.path).await()
        .arrayBuffer().await()
    val buffer = Uint8Array(response)

    return feedType.decode(buffer)
}

const val AUTH_STORAGE_KEY = "streetlight.auth"
private var authCache: Auth? = null

fun readAuth() = authCache ?: localStorage.getItem(AUTH_STORAGE_KEY)?.let { value ->
    Json.decodeFromString<Auth?>(value).also { authCache = it }
}

fun writeAuth(auth: Auth) {
    authCache = auth
    localStorage.setItem(AUTH_STORAGE_KEY, Json.encodeToString(auth))
}

suspend fun <T> AppContext.authRequest(
    method: String,
    path: String,
    body: String? = null,
    fetchWithJwt: suspend (String?) -> Response = { jwt ->
        window.fetch(
            path,
            RequestInit(
                method = method,
                headers = json(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $jwt"
                ),
                body = body
            )
        ).await()
    },
    block: suspend (Response) -> T
): T? {
    var auth = readAuth()
    var response = fetchWithJwt(auth?.jwt)

    if (response.status == 401.toShort()) {
        console.log("authorizing")
        val loginResponse = window.fetch(
            UserApi.Login.path,
            RequestInit(
                method = "POST",
                headers = json(
                    "Content-Type" to "application/json",
                ),
                body = Json.encodeToString(
                    gate.getLoginRequest()
                ),
            )
        ).await()

        if (!loginResponse.ok) {
            console.log("Login failed")
            return null
        }

        val loginText = loginResponse.text().await()
        auth = Json.decodeFromString<Auth>(loginText)
        writeAuth(auth)

        response = fetchWithJwt(auth.jwt)
    }

    return block(response)
}

suspend fun AppContext.uploadBlob(postUrl: String, blobUrl: String): String? {
    return authRequest(
        method = "POST",
        path = postUrl,
        body = null,
        fetchWithJwt = { jwt ->
            val response = window.fetch(blobUrl).await()
            val blob: Blob = response.blob().await()

            window.fetch(
                postUrl,
                RequestInit(
                    method = "POST",
                    headers = json(
                        "Content-Type" to blob.type.ifEmpty { "application/octet-stream" },
                        "Authorization" to "Bearer $jwt"
                    ),
                    body = blob
                )
            ).await()
        }
    ) {
        it.text().await()
    }
}