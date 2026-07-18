package streetlight.web.model

import kampfire.api.UserApi
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestCredentials
import org.w3c.fetch.RequestInit
import org.w3c.fetch.SAME_ORIGIN
import kotlin.js.json

// class AuthClient(
//     private val cred: CredentialStore
// ) {
//     suspend fun authenticate(): Boolean  {
//         val loginRequest = cred.getLoginRequest()
//         if (loginRequest == null) {
//             console.log("credentials not found")
//             return false
//         }
//
//         console.log("authorizing")
//
//         val loginResponse = window.fetch(
//             UserApi.Login.path,
//             defaultRequest(
//                 method = "POST",
//                 headers = json(
//                     "Content-Type" to "application/json"
//                 ),
//                 body = Json.encodeToString(loginRequest)
//             )
//         ).await()
//
//         if (!loginResponse.ok) {
//             console.log("Login failed")
//             return false
//         }
//
//         cred.followUpAuth(loginResponse.ok)
//
//         return true
//     }
// }