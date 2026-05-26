package streetlight.model.data

import kampfire.api.Slug
import kampfire.model.GeoPoint
import kampfire.model.Url
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LocationPost(
    override val postId: PostId,
    override val slug: Slug,
    override val galaxyId: GalaxyId,
    override val username: String?,
    override val userThumb: Url?,
    val location: Location,
    override val text: String?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): GalaxyPost {
    override val images get() = location.images
    override val geoPoint get() = location.geoPoint ?: GeoPoint.Denver
    override val description get() = location.description
    override val visibility get() = 0
    override val title get() = location.name ?: "[location removed]"
    override val links get() = location.extraLinks

    override val postType get() = PostType.Location
}

@Serializable
data class LocationPostEdit(
    val postId: PostId?,
    val galaxyId: GalaxyId,
    val locationId: LocationId,
    val text: String? = null,
) {
    val isValid get () = true // !title.isNullOrBlank()
}
