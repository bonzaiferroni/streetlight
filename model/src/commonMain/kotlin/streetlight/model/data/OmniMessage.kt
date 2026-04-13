package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed interface OmniMessage

interface OmniRecord {
    val recordAt: Instant
    val text: String
}

@Serializable
data class EventPosted(
    val eventId: EventId,
    val galaxyId: GalaxyId,
    val title: String,
    val galaxy: String,
    val username: String,
    override val recordAt: Instant
): OmniMessage, OmniRecord {
    override val text get() = "$username posted an event to $galaxy: $title"
}

@Serializable
data class EventEdited(
    val eventId: EventId,
    val title: String,
    val username: String,
    // td: add edit note
    override val recordAt: Instant
): OmniMessage, OmniRecord {
    override val text get() = "$username edited an event: $title"
}

@Serializable
data class OmniStatus(val starCount: Int): OmniMessage