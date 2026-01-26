package streetlight.model.data

import kampfire.model.GeoPoint
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EventLocation(
    val eventId: EventId,
    val locationId: LocationId,
    val url: String?,
    val imageUrl: String?,
    val title: String,
    val description: String?,
    val status: EventStatus,
    val eventType: EventType,
    val startsAt: Instant,
    val endsAt: Instant,
    val geoPoint: GeoPoint,
) {
    companion object {
        fun from(event: Event, location: Location) = EventLocation(
            eventId = event.eventId,
            locationId = location.locationId,
            url = event.url,
            imageUrl = event.imageUrl,
            title = event.title,
            description = event.description,
            status = event.status,
            eventType = event.eventType,
            startsAt = event.startsAt,
            endsAt = event.endsAt,
            geoPoint = location.geoPoint,
        )
    }
}