package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kotlin.time.Instant

sealed interface StarPost {
    val postId: StarPostId
    val galaxyId: GalaxyId?
    val username: String?
    val text: String?
    val images: ScaledImageArray?
    val geoPoint: GeoPoint?
    val description: String?
    val title: String
    val visibility: Int
    val createdAt: Instant
    val updatedAt: Instant

    val isRemoved: Boolean
    val type: PostType
}

sealed interface StarPostId {
    val stringId: String
}