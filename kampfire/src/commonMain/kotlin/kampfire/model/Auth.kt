package kampfire.model

import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    val jwt: Token,
    val refreshToken: Token,
)

@Serializable
data class Token(
    val value: String,
    val maxAgeSeconds: Int,
)