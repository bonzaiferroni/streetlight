package streetlight.web

import kampfire.api.Endpoint
import kampfire.api.GetByTableIdEndpoint
import kampfire.api.GetEndpoint
import kampfire.api.PathBuilder
import kampfire.api.PostEndpoint
import kampfire.api.QueryEndpoint
import kampfire.api.TableId
import kampfire.api.UserApi
import kampfire.model.Auth
import kampfire.model.LoginRequest
import koala.external.FeedMessage
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array
import org.w3c.dom.WebSocket
import org.w3c.dom.get
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import org.w3c.files.Blob
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
        authRequest("GET", resolvePath(endpoint, block) ) { handleResponse(it) }

    suspend inline fun <Id: TableId<*>, reified Returned> get(
        endpoint: GetByTableIdEndpoint<Id, Returned>,
        id: Id
    ): Returned? =
        authRequest("GET", "${endpoint.path}/${id.value}") { handleResponse(it) }

    suspend inline fun <reified Sent, reified Returned> get(
        endpoint: QueryEndpoint<Sent, Returned>,
        query: String?
    ): Returned? {
        val url = if (!query.isNullOrEmpty()) "${endpoint.path}?$query" else endpoint.path
        return authRequest("GET", url) { handleResponse(it) }
    }

    suspend inline fun <reified Sent, reified Returned> post(
        endpoint: PostEndpoint<Sent, Returned>,
        body: Sent,
    ): Returned? = authRequest("POST", endpoint.path, Json.encodeToString(body)) { handleResponse(it) }

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

    private var authCache: Auth? = null

    fun <E: Endpoint<*, *>> resolvePath(
        endpoint: E,
        block: (PathBuilder.(E) -> Unit)? = null
    ): String {
        val block = block ?: return endpoint.path
        val builder = PathBuilder(endpoint)
        builder.block(endpoint)
        return builder.build()
    }

    fun readAuth() = authCache ?: localStorage.getItem(AUTH_STORAGE_KEY)?.let { value ->
        Json.decodeFromString<Auth?>(value).also { authCache = it }
    }

    fun writeAuth(auth: Auth) {
        authCache = auth
        localStorage.setItem(AUTH_STORAGE_KEY, Json.encodeToString(auth))
    }

    suspend fun <T> authRequest(
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
            writeAuth(auth)

            response = fetchWithJwt(auth.jwt)
        }

        return block(response)
    }

    suspend fun uploadBlob(postUrl: String, blobUrl: String): String? {
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

    suspend inline fun <reified Returned> handleResponse(response: Response): Returned? {
        val text = response.text().await()

        return when (Returned::class) {
            String::class -> text as Returned

            Int::class -> text.toIntOrNull() as Returned?
            Long::class -> text.toLongOrNull() as Returned?

            Double::class -> text.toDoubleOrNull() as Returned?
            Float::class -> text.toFloatOrNull() as Returned?
            Boolean::class -> text.toBooleanStrictOrNull() as Returned?

            else -> Json.decodeFromString<Returned>(text)
        }
    }

    private fun getLoginRequest(): LoginRequest? {
        val usernameOrEmail = localStorage[USERNAME_KEY] ?: return null
        val stayLoggedIn = localStorage[STAY_LOGGED_KEY]?.toBooleanStrictOrNull() ?: false
        val password = localStorage[PASSWORD_KEY] ?: return null
        return LoginRequest(
            usernameOrEmail = usernameOrEmail,
            stayLoggedIn = stayLoggedIn,
            password = password,
        )
    }
}

const val AUTH_STORAGE_KEY = "streetlight.auth"