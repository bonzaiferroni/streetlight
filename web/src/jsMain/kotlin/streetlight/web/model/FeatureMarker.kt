package streetlight.web.model

import kabinet.utils.toRelativeDayFormat
import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.Url
import kampfire.model.thumb
import koala.SiteImage
import koala.SvgFile
import koala.css.*
import koala.html.column
import koala.html.heading5
import koala.html.span
import koala.html.textBlock
import koala.model.IconMarker
import koala.model.MarkerId
import koala.model.ThumbMarker
import kotlinx.css.rgb
import kotlinx.html.DIV
import streetlight.model.data.City
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.Media
import streetlight.web.layouts.ColorScheme

interface FeatureMarker: ThumbMarker {
    val markerType: MarkerType
    override val label: String
    val sublabel: String? get() = null
    override val zIndex get() = 1
    override val labelContent: DIV.() -> Unit get() = {
        column(modify(GapTiny, LineHeight115, WhiteSpaceNoWrap)) {
            setStyle(Property.ColorScheme.to(markerType.colorScheme.cssValue))
            heading5(label)
            textBlock {
                span(markerType.label, modify(ColorSchemeFg, Bold))
                sublabel?.let {
                    span(" • ", modify(OpacityHalf))
                    span(it)
                }
            }
        }
    }
}

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
): FeatureMarker {
    override val markerId get() = location.locationId.value.toString()
    override val label get() = location.label
    override val geoPoint get() = location.geoPoint
    override val thumbUrl get() = location.images.thumb ?: SiteImage.placeholderTh.url
    override val light get() = rgb(180, 240, 100)
    override val markerType get() = MarkerType.Location
}

data class EventMarker(
    val event: EventLocation,
): FeatureMarker {
    override val markerId get() = event.eventId.value.toString()
    override val label get() = event.label
    override val sublabel get() = event.startsAt.toRelativeDayFormat()
    override val thumbUrl get() = event.images.thumb ?: SiteImage.placeholderTh.url
    override val light get() = rgb(240, 100, 180 )
    override val geoPoint get() = event.geoPoint
    override val markerType get() = MarkerType.Event
}

data class GalaxyMarker(
    val galaxy: Galaxy
): FeatureMarker {
    override val markerId get() = galaxy.galaxyId.string
    override val label get() = galaxy.name
    override val thumbUrl get() = galaxy.images.thumb ?: SiteImage.placeholderTh.url
    override val geoPoint get() = galaxy.geoPoint
    override val markerType get() = MarkerType.Galaxy
}

data class MediaMarker(
    val media: Media,
    override val geoPoint: GeoPoint
): FeatureMarker {
    override val markerType get() = MarkerType.Media
    override val label get() = media.label
    override val thumbUrl get() = media.images.thumb ?: SiteImage.placeholderTh.url
    override val markerId get() = media.mediaId.value.toString()

}

data class CityMarker(
    val city: City
): FeatureMarker {
    override val label get() = "${city.name}, ${city.state}"
    override val markerId get() = city.cityId.toString()
    override val thumbUrl get() = SvgFile.City.url
    override val geoPoint get() = city.geoPoint
    override val markerType get() = MarkerType.City
}