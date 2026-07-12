package kampfire.model

import kampfire.api.TableId
import kampfire.api.Username
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

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

data class Identity(
    val callerId: CallerId,
    val username: Username,
    val roles: Set<UserRole>,
)

@JvmInline
value class CallerId(override val value: Uuid): TableId<Uuid>

data class SessionIdentity(
    val session: Session,
    val identity: Identity,
)
