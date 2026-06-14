package streetlight.model.data

import kampfire.api.TableId
import kampfire.api.Username
import kampfire.model.AuthUser
import kampfire.model.UserRole
import kotlin.time.Instant

data class StarUser(
    val starId: StarId,
    override val username: Username,
    override val hashedPassword: String,
    override val salt: String,
    override val email: String?,
    override val roles: Set<UserRole>,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): AuthUser {
    override val userId get() = starId
}