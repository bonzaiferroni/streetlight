package kampfire.model

import kotlinx.serialization.Serializable

/** The result of a sign-up, with a [message] for the user. */
@Serializable
data class SignUpResult(
    val isSuccess: Boolean,
    val message: String
)