package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomUuidString
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Galaxy(
    val galaxyId: GalaxyId,
    val pathId: String,
    val name: String,
    val description: String?,
    val center: GeoPoint,
    val imageUrl: String?,
    val thumbUrl: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class GalaxyId(override val value: String): ProjectId {
    companion object { fun random() = GalaxyId(randomUuidString())}
}

@Serializable
data class GalaxyEdit(
    val name: String? = null,
    val description: String? = null,
    val center: GeoPoint? = null,
    val imageUrl: String? = null,
    val thumbUrl: String? = null,
) {
    val isValid get() = !name.isNullOrBlank() && center != null
}