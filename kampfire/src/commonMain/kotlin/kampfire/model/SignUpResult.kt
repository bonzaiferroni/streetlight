package kampfire.model

import kotlinx.serialization.Serializable

@Serializable
data class SignUpResult(
    val isSuccess: Boolean,
    val message: String
)