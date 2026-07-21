package streetlight.web.model

import kampfire.model.AccountType
import kampfire.model.LoginRequest
import kampfire.model.Messenger
import kampfire.model.PrintLnMessenger
import kampfire.model.handleResponse
import koala.dom.launch
import koala.model.dedup
import koala.model.fieldOf
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

    val starField = state.fieldOf { it.star }
    val signedInFlow = stateFlow.dedup { it.isSignedIn }
    val signedOutAtFlow = stateFlow.dedup { it.signedOutAt }

    fun signIn(receiver: Messenger?) {
        if (stateNow.star != null) return
        console.log("signing in")
        scope.launch {
            readUser(receiver)
        }
    }

    fun signIn(request: LoginRequest, receiver: Messenger) {
        scope.launch("sign-in") {
            if (api.login(request).handleResponse(receiver) ?: false) {
                readUser(receiver)
            }
        }
    }

    suspend fun readUser(receiver: Messenger?) {
        api.validateLogin().handleResponse(receiver ?: PrintLnMessenger) { star ->
            val star = star ?: return@handleResponse
            console.log("signed in: ${star.accountType}")
            state.set { copy(star = star) }

            // guest check in
            if (star.accountType == AccountType.Guest) {
                scope.launch("StarSession > check-guest") {
                    api.checkGuest()
                }
            }
        }
    }

    fun signOut() {
        scope.launch(::signOut) {
            api.logout()
            // userCache.reset()
            state.setValue { it.copy(star = null, signedOutAt = Clock.System.now()) }
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
