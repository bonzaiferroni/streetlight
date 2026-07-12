package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Username
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
    val description: Markdown?,
    val scoutLevel: Int,
    val image: Image?,
    val updatedAt: Instant,
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
    val username: Username? = null,
    val name: String? = null,
    val description: Markdown? = null,
    val image: Image? = null,
)

fun Star.toEdit() = StarEdit(
    username = username,
    name = name,
    description = description,
    image = image
)

fun CallerId.toStarId() = StarId(value)
val Identity.starId get() = callerId.toStarId()