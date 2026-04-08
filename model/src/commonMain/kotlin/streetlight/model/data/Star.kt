package streetlight.model.data

import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kampfire.utils.randomUuidString
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Star(
    val name: String,
    val description: String?,
    val imageRef: Url?,
    val images: ScaledImageArray?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class StarId(override val value: String): ProjectId {
    companion object { fun random() = StarId(randomUuidString())}
    override fun toString() = value
}

@Serializable
data class StarEdit(
    val name: String? = null,
    val description: String? = null,
    val imageRef: Url? = null,
)