package streetlight.model.data

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.time.Instant

@Serializable
sealed interface OmniMessage

@Serializable
sealed interface OmniRecord: OmniMessage {
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
): OmniRecord {
    override val text get() = "$username posted an event to $galaxy: $title"
}

@Serializable
data class EventEdited(
    val eventId: EventId,
    val title: String,
    val username: String,
    // td: add edit note
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$username edited an event: $title"
}

@Serializable
data class LocationCreated(
    val locationId: LocationId,
    val name: String,
    val username: String?,
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$username created a location: $name"
}

@Serializable
data class LocationEdited(
    val locationId: LocationId,
    val name: String,
    val username: String?,
    // td: add edit note
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$username edited a location: $name"
}

@Serializable
data class GalaxyFounded(
    val galaxyId: GalaxyId,
    val name: String,
    val username: String,
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$username founded a galaxy: $name"
}

@Serializable
data class OmniStatus(val starCount: Int): OmniMessage

@Serializable
data class OmniHistory(val records: List<OmniRecord>): OmniMessage

@Serializable
data class EventLighted(
    val eventId: EventId,
    val title: String,
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$title got a little brighter."
}