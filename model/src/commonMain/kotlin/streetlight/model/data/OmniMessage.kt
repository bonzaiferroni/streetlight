package streetlight.model.data

import kampfire.api.Slug
import kampfire.api.Username
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
    val slug: Slug,
    val title: String,
    val username: Username,
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$username posted an event: $title"
}

@Serializable
data class EventUpdated(
    val slug: Slug,
    val title: String,
    val username: Username,
    // td: add edit note
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$username edited an event: $title"
}

@Serializable
data class LocationCreated(
    val locationId: LocationId,
    val name: String,
    val username: Username?,
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$username created a location: $name"
}

@Serializable
data class LocationEdited(
    val locationId: LocationId,
    val name: String,
    val username: Username?,
    // td: add edit note
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$username edited a location: $name"
}

@Serializable
data class GalaxyFounded(
    val slug: Slug,
    val name: String,
    val username: Username,
    override val recordAt: Instant
): OmniRecord {
    override val text get() = "$username founded a galaxy: $name"
}

@Serializable
data class MessageSent(val username: Username, val sentAt: Instant): OmniRecord {
    override val text get() = "$username sent you a message"
    override val recordAt get() = sentAt
}

@Serializable
data class OmniHistory(val records: List<OmniRecord>): OmniMessage

