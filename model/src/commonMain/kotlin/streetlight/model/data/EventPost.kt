package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class EventPost(
    override val postId: EventPostId,
    override val galaxyId: GalaxyId,
    override val username: String?,
    override val location: Location?,
    override val event: Event?,
    override val text: String?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): MapPost {
    override val thumbUrl get() = event?.thumbUrl ?: location?.thumbUrl
    override val imageUrl get() = event?.imageUrl ?: location?.imageUrl
    override val geoPoint get() = location?.geoPoint ?: GeoPoint.Denver
    override val title get() = event?.title ?: "[event removed]"
    override val description get() = event?.description
    override val visibility get() = 0

    override val isRemoved get() = event == null
}

@Serializable
@JvmInline
value class EventPostId(override val value: String): ProjectId, MapPostId {
    override val stringId get() = value
    companion object {
        fun random() = EventPostId(randomUuidString())
    }
}

@Serializable
data class EventPostRow(
    val postId: EventPostId,
    val galaxyId: GalaxyId,
    val eventId: EventId?,
    val userId: UserId?,
    val username: String?,
    val text: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
data class EventPostEdit(
    val postId: EventPostId? = null,
    val galaxyId: GalaxyId? = null,
    val eventId: EventId? = null,
    val text: String? = null,
) {
    val isValid get () = true // !title.isNullOrBlank()
}