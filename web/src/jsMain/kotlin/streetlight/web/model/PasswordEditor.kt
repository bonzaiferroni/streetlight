package streetlight.web.model

import kampfire.api.Password
import kampfire.api.toValidOutcome
import kampfire.model.Outcome
import kampfire.model.Problem
import koala.model.tap
import koala.model.storeOf

class PasswordEditor() {
    private val state = storeOf(PasswordEditorState())
    private val stateNow get() = state.now

    val passwordFlow = state.flow.tap { it.password }
    val confirmationFlow = state.flow.tap { it.confirmation }
    val isValidFlow = state.flow.tap { it.isValid }

    fun setPassword(value: String) = state.set { it.copy(password = value) }
    fun setConfirmation(value: String) = state.set { it.copy(confirmation = value) }

    fun getOutcome(): Outcome<Password> = when {
        stateNow.password != stateNow.confirmation -> Problem("Password input does not match")
        else -> Password(stateNow.password).toValidOutcome()
    }
}

data class PasswordEditorState(
    val password: String = "",
    val confirmation: String = "",
) {
    val isValid get() = password == confirmation
}