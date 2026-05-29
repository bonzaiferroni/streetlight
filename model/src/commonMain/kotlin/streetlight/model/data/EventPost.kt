package streetlight.model.data

import kampfire.api.Slug
import kampfire.model.Url
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EventPost(
    override val postId: PostId,
    override val slug: Slug,
    override val galaxyId: GalaxyId,
    override val username: String?,
    val event: EventLocation,
    override val text: String?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): GalaxyPost {
    override val images get() = event.images ?: event.images
    override val geoPoint get() = event.geoPoint
    override val title get() = event.title
    override val description get() = event.description
    override val boosts get() = 0
    override val links get() = event.links

    override val postType get() = PostType.Event
}

@Serializable
data class EventPostEdit(
    val postId: PostId? = null,
    val galaxyId: GalaxyId,
    val eventId: EventId,
    val text: String? = null,
) {
    val isValid get () = true // !title.isNullOrBlank()
}