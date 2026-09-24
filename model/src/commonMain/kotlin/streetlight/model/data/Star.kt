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

/** A user of the site, as others see them. */
@Serializable
data class Star(
    override val username: Username,
    val roles: Set<UserRole>,
    val name: String?,
    val tagline: String?,
    val description: Markdown?,
    val scoutLevel: Int,
    override val image: Image?,
    val design: PageDesign?,
    val accountType: AccountType,
    override val createdAt: Instant,
): Entity {
    val isAdmin get() = roles.contains(UserRole.Admin)

    override val label get() = username.value
    override val sublabel get() = tagline
    override val body get() = description
    override val geoPoint get() = null
    override val markerId get() = null
}

@JvmInline
@Serializable
value class StarId(override val value: Uuid): RecordId {
    companion object { fun random() = StarId(Uuid.random())}
    override fun toString() = value.toString()
}

/** The fields of a user's profile a form sends. */
@Serializable
data class StarEdit(
    val username: Username?,
    val tagline: String?,
    val description: Markdown?,
    val image: Image?,
    val design: PageDesign?,
)

/** An edit of this user's profile, starting from its current values and [design]. */
fun Star.toEdit(design: PageDesign?) = StarEdit(
    username = username,
    tagline = tagline,
    description = description,
    image = image,
    design = design
)

/** A user's name and thumbnail, as a badge shows them. */
@Serializable
data class StarBadge(
    val username: Username,
    val thumb: Url?,
)

/** The content of the profile config page. */
@Serializable
data class ProfileConfig(
    val design: PageDesign?
): RouteContent