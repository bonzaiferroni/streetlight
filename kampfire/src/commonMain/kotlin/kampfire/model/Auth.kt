package kampfire.model

import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    val jwt: String,
    val refreshToken: String,
)