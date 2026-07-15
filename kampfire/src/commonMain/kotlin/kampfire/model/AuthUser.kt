package kampfire.model

import kampfire.api.Email
import kampfire.api.HashedPassword
import kampfire.api.TableId
import kampfire.api.Username
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class UserRecord(
    val userId: TableId<Uuid>,
    val username: Username,
    val hashedPassword: HashedPassword,
    val email: String?,
    val roles: Set<UserRole>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Deprecated("Use UserRecord")
interface AuthUser {
    val userId: TableId<Uuid>
    val username: Username
    val hashedPassword: HashedPassword
    val email: Email?
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
    val hashedPassword: HashedPassword,
    val roles: Set<UserRole>,
    val accountType: AccountType,
)

enum class AccountType {
    Guest,
    Registered
}