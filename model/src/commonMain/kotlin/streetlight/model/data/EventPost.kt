package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomUuidString
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class EventPost(
    override val postId: EventPostId,
    override val galaxyId: GalaxyId,
    val event: EventLocation?,
    override val text: String?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): StarPost {
    override val images get() = event?.images ?: event?.images
    override val geoPoint get() = event?.geoPoint
    override val title get() = event?.title ?: "[event removed]"
    override val description get() = event?.description
    override val visibility get() = 0
    override val username get() = event?.username

    override val isRemoved get() = event == null
    override val type get() = PostType.Event
}

@Serializable
@JvmInline
value class EventPostId(override val value: String): ProjectId, StarPostId {
    override val stringId get() = value
    companion object {
        fun random() = EventPostId(randomUuidString())
    }
}

@Serializable
data class EventPostRow(
    val postId: EventPostId,
    val galaxyId: GalaxyId,
    val eventId: EventId,
    val starId: StarId?,
    val text: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
data class EventPostEdit(
    val postId: EventPostId? = null,
    val galaxyId: GalaxyId? = null,
    val eventId: EventId? = null,
    val text: String? = null,
) {
    val isValid get () = true // !title.isNullOrBlank()
}