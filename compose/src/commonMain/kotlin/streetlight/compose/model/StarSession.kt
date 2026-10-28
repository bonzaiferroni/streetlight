package streetlight.compose.model

import kampfire.api.Password
import kampfire.api.obfuscatePassword
import kampfire.model.LoginRequest
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Tap
import kampfire.model.toDataOr
import kampfire.model.storeOf
import streetlight.model.data.Star
import streetlight.model.io.ApiClient

/**
 * The star signed in on this device, `null` while signed out. A session stays signed in until the star signs out, and
 * its cookie outlives the app. The cousin of the web's `SessionClient`.
 */
class StarSession(private val api: ApiClient) {
    private val store = storeOf<Star?>(null)

    val starState: Tap<Star?> = store

    /** Reads the star of the saved session, if it is still open. */
    suspend fun restore() {
        store.set((api.star.validateLogin() as? Ok)?.data)
    }

    /** Signs in with the username or email [identity] and [password], then reads the star. */
    suspend fun signIn(identity: String, password: String): Outcome<Star> {
        val request = LoginRequest(identity, isTemp = false, password = Password(password).obfuscatePassword())
        api.user.login(request).toDataOr { return it }
        val star = api.star.validateLogin()
        store.set((star as? Ok)?.data)
        return star
    }

    /** Signs out, closing the session on the server. */
    suspend fun signOut() {
        api.user.logout()
        store.set(null)
    }
}
