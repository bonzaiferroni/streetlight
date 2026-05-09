package streetlight.web.model

import kampfire.api.UserApi
import kampfire.model.Auth
import kotlinx.browser.window
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import kotlin.js.json

class AuthClient(
    private val cred: StarCred
) {
    private val authMutex = Mutex()
    private var inFlightAuth: Deferred<Boolean?>? = null

    // td: should be clearer
    suspend fun authenticate(): Boolean  {
        val loginRequest = cred.getLoginRequest()
        if (loginRequest == null) {
            console.log("credentials not found")
            return false
        }

        console.log("authorizing")

        val loginResponse = window.fetch(
            UserApi.Login.path,
            RequestInit(
                method = "POST",
                headers = json(
                    "Content-Type" to "application/json"
                ),
                body = Json.encodeToString(loginRequest)
            )
        ).await()

        if (!loginResponse.ok) {
            console.log("Login failed")
            return false
        }

        return true
    }
}