package streetlight.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EventPost(
    val postId: EventPostId,
    val galaxyId: GalaxyId,
    val username: String?,
    val location: Location,
    val event: Event,
    val text: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val thumbUrl get() = event.thumbUrl ?: location.thumbUrl
    val imageUrl get() = event.imageUrl ?: location.imageUrl
    val geoPoint get() = location.geoPoint
    val description get() = event.description
    val title get() = event.title
    val visibility get() = 0
}