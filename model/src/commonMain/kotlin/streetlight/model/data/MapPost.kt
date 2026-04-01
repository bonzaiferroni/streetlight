package streetlight.model.data

import kampfire.model.GeoPoint
import kotlinx.datetime.Instant

sealed interface MapPost {
    val postId: MapPostId
    val galaxyId: GalaxyId
    val username: String?
    val location: Location?
    val event: Event?
    val text: String?
    val thumbUrl: String?
    val imageUrl: String?
    val geoPoint: GeoPoint
    val description: String?
    val title: String
    val visibility: Int
    val createdAt: Instant
    val updatedAt: Instant
}

sealed interface MapPostId {
    val stringId: String
}