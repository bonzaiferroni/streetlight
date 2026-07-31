package kampfire.model

import kampfire.api.EmailAddress
import kampfire.api.PasswordHash
import kampfire.api.TableId
import kampfire.api.Username
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class UserRecord(
    val userId: TableId<Uuid>,
    val username: Username,
    val passwordHash: PasswordHash?,
    val disabledPasswordHash: PasswordHash?,
    val email: EmailAddress?, // probably should not be a property on UserRecord
    val roles: Set<UserRole>,
    val accountType: AccountType,
    val guestToken: HashedToken?,
    val activeAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Deprecated("Use UserRecord")
interface AuthUser {
    val userId: TableId<Uuid>
    val username: Username
    val passwordHash: PasswordHash
    val email: EmailAddress?
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
    val passwordHash: PasswordHash?,
    val roles: Set<UserRole>,
    val accountType: AccountType,
    val guestToken: HashedToken?,
)

enum class AccountType {
    Guest,
    Registered
}