package streetlight.web.model

import kampfire.api.Password
import kampfire.api.toValidOutcome
import kampfire.model.Outcome
import kampfire.model.Problem
import koala.model.fieldOf
import koala.model.mutableFieldOf
import koala.model.storeOf

class PasswordEditor() {
    private val state = storeOf(PasswordEditorState())
    private val stateNow get() = state.now

    val passwordField = state.mutableFieldOf({ it.password }) { copy(password = it) }
    val confirmationFlow = state.mutableFieldOf({ it.confirmation }) { copy(confirmation = it) }
    val isValidFlow = state.fieldOf { it.isValid }

    // fun setPassword(value: String) = state.setValue { it.copy(password = value) }
    // fun setConfirmation(value: String) = state.setValue { it.copy(confirmation = value) }

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