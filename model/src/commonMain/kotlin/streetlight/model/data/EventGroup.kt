package streetlight.model.data

import kampfire.model.GeoPoint
import koala.Image
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class EventGroup(
    val locationId: LocationId,
    val label: String,
    val image: Image,
    val geoPoint: GeoPoint,
    val eventCount: Int,
    val tag: EventTag?,
    val startsAt: Instant?,
)