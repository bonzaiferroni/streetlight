package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.AccountType
import kampfire.model.Url
import kampfire.model.UserRole
import koala.Image
import koala.model.RouteContent
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
    val design: PageDesign?,
    val accountType: AccountType,
    val createdAt: Instant,
) {
    val isAdmin get() = roles.contains(UserRole.Admin)
}

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
    val design: PageDesign?,
)

fun Star.toEdit(design: PageDesign?) = StarEdit(
    username = username,
    tagline = tagline,
    description = description,
    image = image,
    design = design
)

@Serializable
data class StarBadge(
    val username: Username,
    val thumb: Url?,
)

@Serializable
data class ProfileConfig(
    val design: PageDesign?
): RouteContent