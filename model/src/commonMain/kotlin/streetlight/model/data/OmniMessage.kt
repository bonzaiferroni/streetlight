package streetlight.model.data

import kampfire.api.StringId
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed interface OmniMessage

@Serializable
sealed interface OmniRecord: OmniMessage {
    val recordAt: Instant
    val text: String
}

@Serializable
data class EventCreated(
    val eventId: EventId,
    val title: String,
    val username: String,
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$username posted an event: $title"
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
data class Beacon(val itemId: StringId): OmniMessage