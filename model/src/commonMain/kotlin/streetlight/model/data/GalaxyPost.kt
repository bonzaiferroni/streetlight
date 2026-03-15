package streetlight.model.data

import kampfire.model.GeoPoint
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class GalaxyPost(
    val postId: GalaxyPostId,
    val galaxyId: GalaxyId,
    val username: String?,
    val location: Location?,
    val event: Event?,
    val title: String,
    val text: String?,
    val geoPoint: GeoPoint?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val thumbUrl get() = event?.thumbUrl ?: location?.thumbUrl
    val position get() = geoPoint ?: location?.geoPoint
    val description get() = event?.description ?: location?.description
}