package streetlight.model.data

import kampfire.api.Email
import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.AccountType
import kampfire.model.CallerId
import kampfire.model.Identity
import kampfire.model.PrivateInfo
import kampfire.model.UserRole
import koala.Image
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Star(
    val username: Username,
    val roles: Set<UserRole>,
    val name: String?,
    val tagline: String?,
    val description: Markdown?,
    val scoutLevel: Int,
    val image: Image?,
    val accountType: AccountType,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class StarId(override val value: Uuid): RecordId {
    companion object { fun random() = StarId(Uuid.random())}
    override fun toString() = value.toString()
}

@Serializable
data class StarEdit(
    val username: Username?,
    val name: String?,
    val email: Email?,
    val tagline: String?,
    val description: Markdown?,
    val image: Image?,
    val identityVisibility: IdentityVisibility,
    val accountType: AccountType,
)

fun Star.toEdit(info: IdentityInfo) = StarEdit(
    username = username,
    name = info.name,
    email = info.email,
    tagline = tagline,
    description = description,
    image = image,
    accountType = accountType,
    identityVisibility = info.identityVisibility,
)

fun CallerId.toStarId() = StarId(value)
val Identity.starId get() = callerId.toStarId()

enum class IdentityVisibility {
    Private,
    Contacts,
    Public
}