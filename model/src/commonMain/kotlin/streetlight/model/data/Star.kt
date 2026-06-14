package streetlight.model.data

import kampfire.api.Username
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kampfire.model.UserRole
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Star(
    val username: Username,
    val roles: Set<UserRole>,
    val name: String?,
    val description: String?,
    val imageRef: Url?,
    val images: ScaledImageArray?,
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
    val description: String? = null,
    val imageRef: Url? = null,
)

fun Star.toEdit() = StarEdit(
    username = username,
    name = name,
    description = description,
    imageRef = imageRef
)