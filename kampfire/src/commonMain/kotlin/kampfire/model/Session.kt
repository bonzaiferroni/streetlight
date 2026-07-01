package kampfire.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@Serializable
data class Session(
    val token: Token,
    val maxAgeSeconds: Int
)

@Serializable
@JvmInline
value class Token(val value: String)

@JvmInline
value class HashedToken(val value: String)

@Serializable
data class AuthLegacy(
    val jwt: TokenInfo,
    val refreshToken: TokenInfo,
)

@Serializable
data class TokenInfo(
    val value: Token,
    val maxAgeSeconds: Int,
)

interface Principal

data class SessionPrincipal(
    val principal: Principal,
    val createdAt: Instant,
    val expiresAt: Instant,
)