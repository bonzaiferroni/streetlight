package streetlight.web.model

import kabinet.utils.toFutureFormat
import kampfire.model.GeoPoint
import kampfire.model.Labeled
import koala.SiteImage
import koala.SvgFile
import koala.model.IconMarker
import koala.model.ThumbMarker
import kotlinx.css.rgb
import streetlight.model.data.City
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.Media
import streetlight.web.layouts.ColorScheme

// interface StreetMarker: ThumbMarker {
//     val markerType: MarkerType
//     override val label: String
//     override val zIndex get() = 1
// }

enum class MarkerType(
    label: String? = null,
    val colorScheme: ColorScheme = ColorScheme.Primary
): Labeled {
    Event(colorScheme = ColorScheme.Accent),
    Location,
    Galaxy,
    Media,
    City;

    override val label = label ?: name
}

data class LocationMarker(
    val location: Location,
): IconMarker {
    override val markerId get() = location.locationId.value.toString()
    override val label get() = location.label
    // override val sublabel get() = location.mapType
    override val geoPoint get() = location.geoPoint
    // override val thumbUrl get() = location.images.thumb ?: SiteImage.placeholderTh.url
    override val light get() = rgb(180, 240, 100)
    override val typeLabel get() = location.mapType ?: MarkerType.Location.label
    override val svg get() = location.mapType?.let { MapTypeIcon[it] } ?: SvgFile.MapPin
    override val colorScheme get() = ColorScheme.Location.cssValue
}

data class EventMarker(
    val event: EventLocation,
): ThumbMarker {
    override val markerId get() = event.eventId.value.toString()
    override val label get() = event.label
    override val sublabel get() = event.startsAt?.toFutureFormat()
    override val thumbUrl get() = event.image.thumb ?: SiteImage.placeholderTh
    override val light get() = rgb(240, 100, 180 )
    override val geoPoint get() = event.geoPoint
    override val typeLabel get() = MarkerType.Event.label
    override val colorScheme get() = ColorScheme.Accent.cssValue
}

data class GalaxyMarker(
    val galaxy: Galaxy
): ThumbMarker {
    override val markerId get() = galaxy.galaxyId.string
    override val label get() = galaxy.name
    override val thumbUrl get() = galaxy.image?.thumb ?: SiteImage.placeholderTh
    override val geoPoint get() = galaxy.geoPoint
    override val typeLabel get() = MarkerType.Galaxy.label
    override val colorScheme get() = ColorScheme.Galaxy.cssValue
}

data class MediaMarker(
    val media: Media,
    override val geoPoint: GeoPoint
): ThumbMarker {
    override val typeLabel get() = media.mediaType.label
    override val label get() = media.label
    override val thumbUrl get() = media.image?.thumb ?: SiteImage.placeholderTh
    override val markerId get() = media.mediaId.value.toString()
    override val colorScheme get() = ColorScheme.Media.cssValue
}

data class CityMarker(
    val city: City
): IconMarker {
    override val label get() = "${city.name}, ${city.state}"
    override val markerId get() = city.cityId.toString()
    override val svg get() = SvgFile.City
    override val geoPoint get() = city.geoPoint
    override val typeLabel get() = MarkerType.City.label
    override val colorScheme get() = ColorScheme.City.cssValue
}