package kampfire.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Serializable
data class Session(
    val token: Token,
    val ttlSeconds: Int,
    val expiresAt: Instant
) {
    fun pastHalfLife(): Boolean = (expiresAt - Clock.System.now()) < ttlSeconds.seconds / 2
}

@Serializable
@JvmInline
value class Token(val value: String)

@JvmInline
value class HashedToken(val hash: String)

interface Identity

data class SessionIdentity(
    val session: Session,
    val identity: Identity,
)
