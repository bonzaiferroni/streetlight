package streetlight.model.data

import kampfire.model.GeoPoint
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EventLocation(
    val eventId: EventId,
    val locationId: LocationId,
    val slug: Slug,
    val url: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
    val title: String,
    val description: String?,
    val status: EventStatus,
    val visibility: Int,
    val geoPoint: GeoPoint,
    val locationName: String,
    val startsAt: Instant,
    val endsAt: Instant?,
) {
    companion object {
        fun from(event: Event, location: Location) = EventLocation(
            eventId = event.eventId,
            slug = event.slug,
            locationId = location.locationId,
            url = event.url,
            imageUrl = event.imageMd,
            thumbUrl = event.imageSm ?: location.thumbUrl,
            title = event.title,
            description = event.description,
            status = event.status,
            visibility = (0..20).random(),
            geoPoint = location.geoPoint,
            locationName = location.name,
            startsAt = event.startsAt,
            endsAt = event.endsAt,
        )
    }
}