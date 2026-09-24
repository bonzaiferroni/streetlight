package streetlight.model.data

import kotlinx.serialization.Serializable

/** A location with its events. */
@Serializable
data class LocationInfo(
    val location: Location,
    val events: List<Event>?
) {
    val locationId get() = location.locationId
    val geoPoint get() = location.geoPoint
}