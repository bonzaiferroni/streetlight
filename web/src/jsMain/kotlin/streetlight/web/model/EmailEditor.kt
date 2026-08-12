package streetlight.web.model

import kampfire.api.EmailAddress
import kampfire.api.toEmailAddress
import kampfire.api.toValidOutcome
import kampfire.model.Ok
import kampfire.model.Outcome
import koala.model.Tap
import koala.model.mutableTapOf
import koala.model.reactIn
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope

class EmailEditor(
    private val emailTap: Tap<EmailAddress?>?,
    private val scope: CoroutineScope
) {
    private val state = storeOf(EmailEditorState(emailTap?.now?.value ?: ""))
    val stateNow get() = state.now
    val stateFlow = state.flow

    init {
        emailTap?.reactIn(scope) {
            state.set { copy(emailString = it?.value ?: "") }
        }
    }

    val emailStringField = state.mutableTapOf({ it.emailString }) { copy(emailString = it) }

    fun getOutcome(): Outcome<EmailAddress?> = stateNow.emailString.takeIf { emailTap?.now == null && it.isNotBlank() }
        ?.trim()?.toEmailAddress()?.toValidOutcome()
        ?: Ok(null)
}

data class EmailEditorState(
    val emailString: String,
)


// val email = StateField(stateFlow.mapDistinct { it.email }) { value -> state.set { it.copy(email = value) } }
