package streetlight.model.data

import kampfire.api.Email
import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.AccountType
import kampfire.model.CallerId
import kampfire.model.Identity
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
    val tagline: String?,
    val description: Markdown?,
    val image: Image?,
)

fun Star.toEdit() = StarEdit(
    username = username,
    tagline = tagline,
    description = description,
    image = image,
)

fun CallerId.toStarId() = StarId(value)
val Identity.starId get() = callerId.toStarId()
