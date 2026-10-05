package streetlight.model.data

import kampfire.model.GeoPoint
import koala.Image
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A location on the map with its next event, as a marker shows it before it is inflated. */
@Serializable
data class EventGroup(
    val locationId: LocationId,
    override val label: String,
    override val image: Image?,
    override val geoPoint: GeoPoint,
    val eventCount: Int,
    val tag: EventTag?,
    val startsAt: Instant?,
    val mapPriority: Double?,
): Entity {
    override val markerId get() = locationId.toString()

    /** The value an [EntityCursor.Score] pages by. */
    val score get() = mapPriority
}
