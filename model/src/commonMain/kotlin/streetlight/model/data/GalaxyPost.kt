package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class GalaxyPost(
    val galaxyPostId: GalaxyPostId,
    val galaxyId: GalaxyId,
    val username: String?,
    val eventId: EventId?,
    val locationId: LocationId?,
    val text: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class GalaxyPostId(override val value: String) : ProjectId {
    companion object {
        fun random() = GalaxyPostId(randomUuidString())
    }
}

@Serializable
data class GalaxyPostEdit(
    val galaxyPostId: GalaxyPostId? = null,
    val galaxyId: GalaxyId? = null,
    val username: String? = null,
    val eventId: EventId? = null,
    val locationId: LocationId? = null,
    val text: String? = null,
)