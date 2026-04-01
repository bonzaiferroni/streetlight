package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class LocationPost(
    override val postId: EventPostId,
    override val galaxyId: GalaxyId,
    override val username: String?,
    override val location: Location,
    val postTitle: String?,
    override val text: String?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): MapPost {
    override val thumbUrl get() = location.thumbUrl
    override val imageUrl get() = location.imageUrl
    override val geoPoint get() = location.geoPoint
    override val description get() = location.description
    override val visibility get() = 0
    override val event: Event? get() = null
    override val title get() = postTitle ?: location.name
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
    val galaxyId: GalaxyId,
    val username: String?,
    val locationId: LocationId,
    val title: String?,
    val text: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
data class LocationPostEdit(
    val postId: LocationPostId? = null,
    val galaxyId: GalaxyId? = null,
    val username: String? = null,
    val locationId: LocationId? = null,
    val title: String? = null,
    val text: String? = null,
) {
    val isValid get () = true // !title.isNullOrBlank()
}