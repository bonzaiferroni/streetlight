package streetlight.web.model

import kampfire.api.Password
import kampfire.api.toValidOutcome
import kampfire.model.Outcome
import kampfire.model.Problem
import koala.model.mutableFieldOf
import koala.model.storeOf

class PasswordEditor() {
    private val state = storeOf(PasswordEditorState())
    private val stateNow get() = state.now

    val passwordField = state.mutableFieldOf({ it.password }) { copy(password = it) }
    val confirmationField = state.mutableFieldOf({ it.confirmation }) { copy(confirmation = it) }

    fun clear() {
        state.set { copy(password = "", confirmation = "")}
    }

    fun getOutcome(): Outcome<Password> = when {
        stateNow.password != stateNow.confirmation -> Problem("Password input does not match").also { println("${stateNow.password} ${stateNow.confirmation}") }
        else -> Password(stateNow.password).toValidOutcome()
    }
}

data class PasswordEditorState(
    val password: String = "",
    val confirmation: String = "",
)