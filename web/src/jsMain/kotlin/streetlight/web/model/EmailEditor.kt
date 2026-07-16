package streetlight.web.model

import kampfire.api.Email
import kampfire.api.toEmail
import kampfire.api.toValidOutcome
import kampfire.model.Ok
import kampfire.model.Outcome
import koala.model.tap
import koala.model.storeOf
import kotlinx.coroutines.flow.Flow

class EmailEditor(
    private val initialValue: String
) {
    private val state = storeOf(EmailEditorState(initialValue))
    val stateNow get() = state.now
    val stateFlow = state.flow
    val emailFlow = stateFlow.tap { it.email }

    fun setEmail(value: String) = state.set { it.copy(email = value) }
    fun getOutcome(): Outcome<Email?> = stateNow.email.takeIf { it.isNotBlank() }?.trim()?.toEmail()?.toValidOutcome()
        ?: Ok(null)
}

data class EmailEditorState(
    val email: String,
)


// val email = StateField(stateFlow.mapDistinct { it.email }) { value -> state.set { it.copy(email = value) } }
