package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class LocationPost(
    override val postId: LocationPostId,
    override val galaxyId: GalaxyId?,
    override val username: String?,
    override val location: Location?,
    val postTitle: String?,
    override val text: String?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): GalaxyPost {
    override val thumbUrl get() = location?.thumbUrl
    override val imageUrl get() = location?.imageUrl
    override val geoPoint get() = location?.geoPoint ?: GeoPoint.Denver
    override val description get() = location?.description
    override val visibility get() = 0
    override val title get() = postTitle ?: location?.name ?: "[location removed]"

    override val isRemoved get() = location == null
    override val type get() = PostType.Location
}

@Serializable
@JvmInline
value class LocationPostId(override val value: String): ProjectId, MapPostId {
    override val stringId get() = value
    companion object {
        fun random() = LocationPostId(randomUuidString())
    }
}

@Serializable
data class LocationPostRow(
    val postId: LocationPostId,
    val locationId: LocationId?,
    val userId: UserId?,
    val username: String?,
    val title: String?,
    val text: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
data class NewLocationPost(
    val locationId: LocationId,
    val title: String? = null,
    val text: String? = null,
)

@Serializable
data class NewGalaxyLocationPost(
    val postId: LocationPostId,
    val galaxyIds: List<GalaxyId>
) {
    val isValid get() = galaxyIds.isNotEmpty()
}

@Serializable
data class GalaxyPostResult(
    val results: Map<GalaxyId, PostResult>
)
