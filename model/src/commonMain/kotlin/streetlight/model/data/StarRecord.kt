package streetlight.model.data

import kampfire.api.Email
import kampfire.api.HashedPassword
import kampfire.api.Username
import kampfire.model.AuthUser
import kampfire.model.UserRole
import kotlin.time.Instant

data class StarRecord(
    val starId: StarId,
    override val username: Username,
    override val hashedPassword: HashedPassword,
    override val email: Email?,
    override val roles: Set<UserRole>,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): AuthUser {
    override val userId get() = starId
}