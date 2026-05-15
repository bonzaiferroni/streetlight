package kampfire.model

import kampfire.api.TableId
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface AuthUser {
    val userId: TableId<Uuid>
    val username: String
    val hashedPassword: String
    val salt: String
    val email: String?
    val roles: Set<UserRole>
    val createdAt: Instant
    val updatedAt: Instant
}

val AuthUser.isAdmin: Boolean
    get() = UserRole.Admin in roles

val AuthUser.isUser: Boolean
    get() = UserRole.User in roles

data class UserSeed(
    val request: SignUpRequest,
    val salt: String,
    val hashedPassword: String,
    val roles: Set<UserRole>
)