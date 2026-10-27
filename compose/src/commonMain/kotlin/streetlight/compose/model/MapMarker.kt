package streetlight.compose.model

import kabinet.utils.toFutureFormat
import kampfire.model.GeoPoint
import kampfire.model.Url
import koala.Svg
import koala.SvgFile
import koala.model.MarkerId
import streetlight.model.data.City
import streetlight.model.data.Entity
import streetlight.model.data.EntityType
import streetlight.model.data.EventGroup
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.Media
import streetlight.model.data.MediaPost

/** A marker on the map at [geoPoint], with its [label] over its [typeLabel], in the color of its [type]. */
sealed interface MapMarker {
    val markerId: MarkerId
    val geoPoint: GeoPoint
    val label: String
    val typeLabel: String
    val type: EntityType
}

/** A [MapMarker] drawn as the thumbnail at [thumb], with a [sublabel] after its type. */
data class ThumbMarker(
    override val markerId: MarkerId,
    override val geoPoint: GeoPoint,
    override val label: String,
    override val typeLabel: String,
    override val type: EntityType,
    val thumb: Url?,
    val sublabel: String? = null,
) : MapMarker

/** A [MapMarker] of an event, drawn as the thumbnail at [thumb], its [sublabel] after its type, and its [locationName]. */
data class EventMarker(
    override val markerId: MarkerId,
    override val geoPoint: GeoPoint,
    override val label: String,
    override val typeLabel: String,
    val thumb: Url?,
    val sublabel: String?,
    val locationName: String?,
) : MapMarker {
    override val type get() = EntityType.Event
}

/** A [MapMarker] drawn as its [svg]. */
data class IconMarker(
    override val markerId: MarkerId,
    override val geoPoint: GeoPoint,
    override val label: String,
    override val typeLabel: String,
    override val type: EntityType,
    val svg: Svg,
) : MapMarker

/** The marker of this entity on the map, `null` for an entity with no place. The web's `createMarker`. */
fun Entity.toMarker(): MapMarker? {
    val markerId = markerId ?: return null
    return when (this) {
        is EventLocation -> EventMarker(
            markerId, geoPoint, label, tag?.label ?: "Event",
            image.let { it.thumb ?: it.url }, startsAt?.toFutureFormat(), locationName,
        )
        is EventPost -> event.toMarker()
        is Location -> IconMarker(markerId, geoPoint, label, mapType ?: "Location", EntityType.Location, SvgFile.MapPin.small)
        is LocationPost -> location.toMarker()
        is Media -> geoPoint?.let {
            ThumbMarker(markerId, it, label, mediaType.label, EntityType.Media, image?.let { image -> image.thumb ?: image.url })
        }
        is MediaPost -> media.toMarker()
        is City -> IconMarker(markerId, geoPoint, "$name, $state", "City", EntityType.City, SvgFile.City.small)
        is Galaxy -> ThumbMarker(markerId, geoPoint, name, "Galaxy", EntityType.Galaxy, image?.let { it.thumb ?: it.url })
        is EventGroup -> when (startsAt) {
            null -> IconMarker(markerId, geoPoint, label, tag?.label ?: "Event", EntityType.Location, SvgFile.MapPin.small)
            else -> EventMarker(
                markerId, geoPoint, label, tag?.label ?: "Event",
                image?.let { it.thumb ?: it.url }, startsAt?.toFutureFormat(), locationName,
            )
        }
        else -> null
    }
}
