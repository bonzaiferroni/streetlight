package streetlight.web.model

import kampfire.api.Username
import kampfire.model.AccountType
import kampfire.model.LoginRequest
import kampfire.model.MessageReceiver
import kampfire.model.PrintLnReceiver
import kampfire.model.handleResponse
import koala.model.tap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Star
import streetlight.web.io.ApiClient
import kotlin.time.Clock
import kotlin.time.Instant

class StarSession(
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val state = storeOf(StarSessionState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val starFlow = stateFlow.tap { it.star }
    val signedInFlow = stateFlow.tap { it.isSignedIn }
    val signedOutAtFlow = stateFlow.tap { it.signedOutAt }

    fun signIn(receiver: MessageReceiver?) {
        if (stateNow.star != null) return
        console.log("signing in")
        scope.launch {
            readUser(receiver)
        }
    }

    fun signIn(request: LoginRequest, receiver: MessageReceiver) {
        scope.launch {
            if (api.login(request).handleResponse(receiver) ?: false) {
                readUser(receiver)
            }
        }
    }

    suspend fun readUser(receiver: MessageReceiver?) {
        api.validateLogin().handleResponse(receiver ?: PrintLnReceiver) { star ->
            console.log("signed in: ${star.accountType}")
            state.set { it.copy(star = star) }

            // guest check in
            if (star.accountType == AccountType.Guest) {
                scope.launch {
                    runCatching { api.checkGuest() }
                        .onFailure { console.log("guest check-in failed: $it") }
                }
            }
        }
    }

    fun signOut() {
        scope.launch {
            api.logout()
            // userCache.reset()
            state.set { it.copy(star = null, signedOutAt = Clock.System.now()) }
        }
    }
}

data class StarSessionState(
    val star: Star? = null,
    val message: String? = null,
    val signedOutAt: Instant? = null,
) {
    val isSignedIn get() = star != null
}
