package kampfire.model

import kotlinx.serialization.Serializable

@Serializable
data class SignUpResult(
    val success: Boolean = false,
    val message: String = "",
)