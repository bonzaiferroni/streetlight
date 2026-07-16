package kampfire.model

import kampfire.api.Email
import kampfire.api.HashedPassword
import kampfire.api.TableId
import kampfire.api.Username
import kampfire.utils.randomUuidString
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

//@Serializable
//data class UserOld(
//    val userId: UserId,
//    val username: String,
//    val roles: RoleSet,
//    val avatarUrl: String?,
//    val createdAt: Instant,
//    val updatedAt: Instant,
//)

// @Serializable
// data class BasicUser(
//     override val userId: BasicUserId,
//     val name: String?,
//     override val username: Username,
//     override val hashedPassword: HashedPassword,
//     override val email: Email?,
//     override val roles: Set<UserRole>,
//     val avatarUrl: String?,
//     override val createdAt: Instant,
//     override val updatedAt: Instant,
// ): AuthUser

// @Serializable
// data class BasicUserInfo(
//     val username: Username,
//     val roles: RoleSet,
//     val avatarUrl: String?,
//     val createdAt: Instant,
// )

// fun BasicUser.toPrivateInfo() = PrivateInfo(
//     name = this.name,
//     email = this.email,
// )

// @JvmInline @Serializable
// value class BasicUserId(override val value: Uuid): TableId<Uuid> {
//     companion object { fun random() = BasicUserId(Uuid.random()) }
// }