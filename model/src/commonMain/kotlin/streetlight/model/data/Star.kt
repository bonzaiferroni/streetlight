package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Star(
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
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
    val imageUrl: String? = null,
    val thumbUrl: String? = null,
)