package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class EventPostRow(
    val eventPostId: EventPostId,
    val galaxyId: GalaxyId,
    val username: String?,
    val eventId: EventId,
    val text: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class EventPostId(override val value: String) : ProjectId {
    companion object {
        fun random() = EventPostId(randomUuidString())
    }
}

@Serializable
data class EventPostEdit(
    val eventPostId: EventPostId? = null,
    val galaxyId: GalaxyId? = null,
    val username: String? = null,
    val eventId: EventId? = null,
    val text: String? = null,
) {
    val isValid get () = true // !title.isNullOrBlank()
}