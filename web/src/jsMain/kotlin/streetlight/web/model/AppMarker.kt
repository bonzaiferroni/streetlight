package streetlight.web.model

import kampfire.model.Labeled
import kampfire.model.thumb
import koala.SiteImage
import koala.css.Rgb
import koala.dom.RenderContext
import koala.dom.box
import koala.model.PointMarker
import kotlinx.css.rgb
import streetlight.model.data.Galaxy
import streetlight.model.data.EventPost
import streetlight.model.data.Location
import streetlight.web.shells.cardOf
import streetlight.web.ui.eventFocusContent

interface AppMarker: PointMarker {
    val markerType: MarkerType
}

enum class MarkerType(label: String? = null): Labeled {
    Event,
    Location,
    Galaxy;

    override val label = label ?: name
}

data class LocationMarker(
    val location: Location,
): AppMarker {
    override val markerId get() = location.locationId.value.toString()
    // override val label get() = location.name
    override val geoPoint get() = location.geoPoint
    override val thumbUrl get() = location.images.thumb ?: SiteImage.placeholderTh.url
    override val light get() = rgb(180, 240, 100)
    // override val modifiers get() = modify(MarkerUtility.twinkleAboveRaincloud)
    override val markerType get() = MarkerType.Location
}

data class EventMarker(
    val post: EventPost,
): AppMarker {
    override val markerId get() = post.event.eventId.value.toString()
    override val thumbUrl get() = post.images.thumb ?: SiteImage.placeholderTh.url
    override val light get() = rgb(240, 100, 180 )
    override val geoPoint get() = post.event.geoPoint
    override val markerType get() = MarkerType.Event
}

data class GalaxyMarker(
    val galaxy: Galaxy
): AppMarker {
    override val geoPoint get() = galaxy.geoPoint
    override val thumbUrl get() = galaxy.images.thumb
    override val markerId get() = galaxy.galaxyId.string
    override val markerType get() = MarkerType.Galaxy
}