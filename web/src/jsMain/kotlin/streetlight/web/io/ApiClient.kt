package streetlight.web.io

import io.kvision.rest.*
import kotlinx.coroutines.await
import streetlight.model.dto.AuthInfo
import streetlight.model.dto.LoginInfo
import streetlight.web.HTTP_OK
import streetlight.web.baseAddress
import streetlight.web.io.stores.LocalStore
import kotlin.js.Promise

val globalApiClient = ApiClient()

class ApiClient() {
    val restClient = RestClient()
    val localStore = LocalStore()

    var jwt = localStore.jwt

    var loginInfo = LoginInfo(username = localStore.username ?: "", session = localStore.session)

    val apiAddress = "$baseAddress/api/v1"

    val tokenHeaders = jwt?.let {
        if (it.isNotBlank()) {
            { listOf(Pair("Authorization", "Bearer $jwt")) }
        } else {
            null
        }
    }

    inline fun <reified Returned : Any, reified Sent : Any> RestRequestConfig<Returned, Sent>.applyConfig(
        method: HttpMethod,
    ) {
        this.method = method
        this.headers = tokenHeaders
        this.serializer = getSerializer<Sent>()
    }

    inline fun <reified Returned : Any> RestRequestConfig<Returned, dynamic>.applyConfigDynamic(
        method: HttpMethod,
    ) {
        this.method = method
        this.headers = tokenHeaders
    }

    inline fun <reified Returned : Any> request(
        method: HttpMethod,
        endpoint: String,
        crossinline block: RestRequestConfig<Returned, dynamic>.() -> Unit = {},
    ): Promise<RestResponse<Returned>> {
        return restClient.request("$apiAddress$endpoint") {
            applyConfigDynamic(method)
            block()
        }
    }

    inline fun <reified Returned : Any, reified Sent : Any> request(
        method: HttpMethod,
        endpoint: String,
        data: Sent,
        crossinline block: RestRequestConfig<Returned, Sent>.() -> Unit = {},
    ): Promise<RestResponse<Returned>> {
        return restClient.request("$apiAddress$endpoint", data) {
            applyConfig(method)
            block()
        }
    }

    inline fun requestDynamic(
        method: HttpMethod,
        endpoint: String,
        crossinline block: RestRequestConfig<String, dynamic>.() -> Unit = {},
    ): Promise<RestResponse<String>> {
        return restClient.request("$apiAddress$endpoint") {
            applyConfigDynamic(method)
            block()
        }
    }

    inline fun <reified Sent : Any> requestText(
        method: HttpMethod,
        endpoint: String,
        data: Sent,
    ): Promise<RestResponse<String>> {
        return request(method, endpoint, data) {
            responseBodyType = ResponseBodyType.TEXT
        }
    }

    suspend inline fun <reified Returned : Any> authRequest(
        requester: () -> Promise<RestResponse<Returned>>,
    ): RestResponse<Returned> {
        return try {
            requester().await()
        } catch (e: Unauthorized) {
            val authorized = login()
            if (authorized) {
                requester().await()
            } else {
                error("Login failed, unable to reauthorize")
            }
        }
    }

    suspend fun login(): Boolean {
        val response: RestResponse<AuthInfo> =
            request<AuthInfo, LoginInfo>(HttpMethod.POST, "/login", loginInfo).await()
        if (response.response.status == HTTP_OK) {
            console.log("StoreClient.login success")
            response.data.session?.let {
                loginInfo = loginInfo.copy(session = it)
                if (localStore.save == true) {
                    localStore.session = loginInfo.session
                    localStore.username = loginInfo.username
                }
            }
            loginInfo = loginInfo.copy(password = null)
            if (localStore.save == true) {
                localStore.jwt = response.data.jwt
            }
            jwt = response.data.jwt
            return true
        } else {
            console.log("StoreClient.login: ${response.response.statusText}")
            return false
        }
    }

    suspend inline fun <reified T : Any> get(endpoint: String): T = authRequest {
        request<T>(HttpMethod.GET, endpoint)
    }.data

    suspend inline fun <reified Received : Any, reified Sent : Any> post(
        endpoint: String,
        data: Sent
    ): Received = authRequest {
        request<Received, Sent>(HttpMethod.POST, endpoint, data)
    }.data

    suspend inline fun <reified Sent : Any> post(endpoint: String): Boolean = authRequest {
        request<Sent>(HttpMethod.POST, endpoint)
    }.response.status == HTTP_OK

    suspend inline fun <reified Received : Any, reified Sent : Any> put(
        endpoint: String, data: Sent
    ): Received = authRequest {
        request<Received, Sent>(HttpMethod.PUT, endpoint, data)
    }.data

    suspend fun delete(endpoint: String, id: Int): Boolean = authRequest {
        requestDynamic(HttpMethod.DELETE, "$apiAddress$endpoint$id")
    }.response.status == HTTP_OK

    suspend inline fun <reified Received : Any, reified Sent : Any> create(
        endpoint: String,
        data: Sent
    ): Received = authRequest {
        request<Received, Sent>(HttpMethod.POST, endpoint, data)
    }.data

    suspend inline fun <reified T : Any> update(endpoint: String, id: Int, data: T): Boolean =
        authRequest { requestText(HttpMethod.PUT, "$endpoint/$id", data) }
            .response.status == HTTP_OK

    suspend fun login(loginInfo: LoginInfo): Boolean {
        this.loginInfo = loginInfo
        return login()
    }

    fun logout() {
        jwt = null
        localStore.jwt = null
        localStore.session = null
        loginInfo = loginInfo.copy(session = null)
    }
}

//suspend inline fun ApiClient.requestOnly(
//    url: String,
//    crossinline block: RestRequestConfig<String, dynamic>.() -> Unit = {}
//): Boolean {
//    try {
//        // val result = this.request<String>(url, block).await()
//        // return result.response.status == HTTP_OK
//    } catch (e: Exception) {
//        when (e) {
//            is XHRError -> return true
//        }
//        console.log("StoreClient.requestOnly: exception: $e")
//        throw e
//    }
//}