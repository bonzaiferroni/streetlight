package streetlight.model.data

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EventPost(
    override val postId: PostId,
    override val galaxyId: GalaxyId,
    override val username: String?,
    val event: EventLocation?,
    override val text: String?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): Post {
    override val images get() = event?.images ?: event?.images
    override val geoPoint get() = event?.geoPoint
    override val title get() = event?.title ?: "[event removed]"
    override val description get() = event?.description
    override val visibility get() = 0
    override val links get() = event?.links

    override val isRemoved get() = event == null
    override val postType get() = PostType.Event
}

@Serializable
data class EventPostEdit(
    val postId: PostId? = null,
    val galaxyId: GalaxyId? = null,
    val eventId: EventId? = null,
    val text: String? = null,
) {
    val isValid get () = true // !title.isNullOrBlank()
}