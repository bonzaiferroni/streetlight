package kampfire.model

import kampfire.api.TableId
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant

interface AuthUser {
    val userId: UserId
    val username: String
    val hashedPassword: String
    val salt: String
    val email: String?
    val roles: Set<UserRole>
    val createdAt: Instant
    val updatedAt: Instant
}

val AuthUser.isAdmin: Boolean
    get() = UserRole.ADMIN in roles

val AuthUser.isUser: Boolean
    get() = UserRole.USER in roles

@JvmInline @Serializable
value class UserId(override val value: String): TableId<String> {
    companion object { fun random() = UserId(randomUuidString()) }
}

data class UserSeed(
    val request: SignUpRequest,
    val salt: String,
    val hashedPassword: String,
    val roles: Set<UserRole>
)