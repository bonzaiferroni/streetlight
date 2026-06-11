package streetlight.web.model

import kabinet.utils.toRelativeDayFormat
import kampfire.model.Labeled
import kampfire.model.thumb
import koala.SiteImage
import koala.css.*
import koala.dom.*
import koala.html.column
import koala.html.heading5
import koala.html.span
import koala.html.textBlock
import koala.model.ThumbMarker
import kotlinx.css.rgb
import kotlinx.html.DIV
import streetlight.model.data.Galaxy
import streetlight.model.data.EventPost
import streetlight.model.data.Location
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
    Galaxy;

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
    val post: EventPost,
): FeatureMarker {
    override val markerId get() = post.event.eventId.value.toString()
    override val label get() = post.label
    override val sublabel get() = post.event.startsAt.toRelativeDayFormat()
    override val thumbUrl get() = post.images.thumb ?: SiteImage.placeholderTh.url
    override val light get() = rgb(240, 100, 180 )
    override val geoPoint get() = post.event.geoPoint
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