package kampfire.model

import kampfire.api.TableId
import kampfire.utils.randomUuidString
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

//@Serializable
//data class UserOld(
//    val userId: UserId,
//    val username: String,
//    val roles: RoleSet,
//    val avatarUrl: String?,
//    val createdAt: Instant,
//    val updatedAt: Instant,
//)

@Serializable
data class BasicUser(
    override val userId: BasicUserId,
    val name: String?,
    override val username: String,
    override val hashedPassword: String,
    override val salt: String,
    override val email: String?,
    override val roles: Set<UserRole>,
    val avatarUrl: String?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): AuthUser

@Serializable
data class BasicUserInfo(
    val username: String,
    val roles: RoleSet,
    val avatarUrl: String?,
    val createdAt: Instant,
)

fun BasicUser.toPrivateInfo() = PrivateInfo(
    name = this.name,
    email = this.email,
)

@JvmInline @Serializable
value class BasicUserId(override val value: String): TableId<String> {
    companion object { fun random() = BasicUserId(randomUuidString()) }
}