package streetlight.web.model

import kabinet.utils.toFutureFormat
import kampfire.model.GeoPoint
import kampfire.model.Labeled
import koala.SiteImage
import koala.SvgFile
import koala.model.IconMarker
import koala.model.ThumbMarker
import kotlinx.html.DIV
import streetlight.model.data.City
import streetlight.model.data.Entity
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.EventGroup
import streetlight.model.data.Media
import streetlight.web.layouts.ThemeColor
import streetlight.web.layouts.light
import streetlight.web.ui.configureEventMarkerBody

// interface StreetMarker: ThumbMarker {
//     val markerType: MarkerType
//     override val label: String
//     override val zIndex get() = 1
// }

interface EntityMarker {
    val entity: Entity
}

enum class MarkerType(
    label: String? = null,
    val themeColor: ThemeColor = ThemeColor.Primary
): Labeled {
    Event(themeColor = ThemeColor.Accent),
    Location,
    Galaxy,
    Media,
    City;

    override val label = label ?: name
}

data class LocationMarker(
    val location: Location,
): IconMarker, EntityMarker {
    override val markerId get() = location.markerId
    override val label get() = location.label
    // override val sublabel get() = location.mapType
    override val geoPoint get() = location.geoPoint
    // override val thumbUrl get() = location.images.thumb ?: SiteImage.placeholderTh.url
    override val light get() = ThemeColor.Location.light
    override val typeLabel get() = location.mapType ?: MarkerType.Location.label
    override val svg get() = location.mapType?.let { MapTypeIcon[it] } ?: SvgFile.MapPin
    override val themeColor get() = ThemeColor.Location.cssValue
    override val entity get() = location
}

data class EventMarker(
    val event: EventLocation,
): ThumbMarker {
    override val markerId get() = event.markerId
    override val label get() = event.label
    override val sublabel get() = event.startsAt?.toFutureFormat()
    override val thumbUrl get() = event.image.thumb ?: SiteImage.placeholderTh
    override val light get() = ThemeColor.Accent.light
    override val geoPoint get() = event.geoPoint
    override val typeLabel get() = event.tags?.firstOrNull()?.label ?: MarkerType.Event.label
    override val themeColor get() = ThemeColor.Accent.cssValue
    override val configureBody: DIV.() -> Unit get() = { configureEventMarkerBody(this@EventMarker, event.locationName) }
}

data class MediaMarker(
    val media: Media,
    override val geoPoint: GeoPoint
): ThumbMarker {
    override val typeLabel get() = media.mediaType.label
    override val label get() = media.label
    override val thumbUrl get() = media.image?.thumb ?: SiteImage.placeholderTh
    override val markerId get() = media.markerId
    override val themeColor get() = ThemeColor.Media.cssValue
    override val light get() = ThemeColor.Media.light
}

data class CityMarker(
    val city: City
): IconMarker {
    override val label get() = "${city.name}, ${city.state}"
    override val markerId get() = city.markerId
    override val svg get() = SvgFile.City
    override val geoPoint get() = city.geoPoint
    override val typeLabel get() = MarkerType.City.label
    override val themeColor get() = ThemeColor.City.cssValue
    override val light get() = ThemeColor.City.light
}

data class GalaxyMarker(
    val galaxy: Galaxy
): ThumbMarker {
    override val markerId get() = galaxy.markerId
    override val label get() = galaxy.name
    override val thumbUrl get() = galaxy.image?.thumb ?: SiteImage.placeholderTh
    override val geoPoint get() = galaxy.geoPoint
    override val typeLabel get() = MarkerType.Galaxy.label
    override val themeColor get() = ThemeColor.Galaxy.cssValue
    override val light get() = ThemeColor.Galaxy.light
}

fun EventGroup.toMarker() = when (eventCount) {
    0 -> LocationInflateMarker(this)
    else -> EventInflateMarker(this)
}