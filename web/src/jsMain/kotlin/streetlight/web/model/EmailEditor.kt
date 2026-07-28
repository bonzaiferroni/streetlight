package streetlight.web.model

import kampfire.api.Email
import kampfire.api.toEmail
import kampfire.api.toValidOutcome
import kampfire.model.Messenger
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.handleOutcome
import koala.model.Field
import koala.model.MutableField
import koala.model.mutableFieldOf
import koala.model.reactIn
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope

class EmailEditor(
    private val emailField: Field<Email?>?,
    private val scope: CoroutineScope
) {
    private val state = storeOf(EmailEditorState(emailField?.now?.value ?: ""))
    val stateNow get() = state.now
    val stateFlow = state.flow

    init {
        emailField?.reactIn(scope) {
            state.set { copy(emailString = it?.value ?: "") }
        }
    }

    val emailStringField = state.mutableFieldOf({ it.emailString }) { copy(emailString = it) }

    fun getOutcome(): Outcome<Email?> = stateNow.emailString.takeIf { emailField?.now == null && it.isNotBlank() }
        ?.trim()?.toEmail()?.toValidOutcome()
        ?: Ok(null)
}

data class EmailEditorState(
    val emailString: String,
)


// val email = StateField(stateFlow.mapDistinct { it.email }) { value -> state.set { it.copy(email = value) } }
