package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.model.Url
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EventPost(
    val event: EventLocation,
    override val base: Post,
): GalaxyPost {
    override val images get() = event.images ?: event.images
    override val geoPoint get() = event.geoPoint
    override val label get() = event.title
    override val sublabel get() = event.locationLabel
    override val body get() = event.description
    override val links get() = event.links
}

// @Serializable
// data class EventPostEdit(
//     val postId: PostId? = null,
//     val galaxyId: GalaxyId,
//     val eventId: EventId,
//     val text: Markdown? = null,
// ) {
//     val isValid get () = true // !title.isNullOrBlank()
// }