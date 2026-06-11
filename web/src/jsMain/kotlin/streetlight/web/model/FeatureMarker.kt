package streetlight.web.model

import kampfire.model.Labeled
import kampfire.model.thumb
import koala.SiteImage
import koala.model.ThumbMarker
import kotlinx.css.rgb
import streetlight.model.data.Galaxy
import streetlight.model.data.EventPost
import streetlight.model.data.Location

interface FeatureMarker: ThumbMarker {
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
): FeatureMarker {
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
): FeatureMarker {
    override val markerId get() = post.event.eventId.value.toString()
    override val thumbUrl get() = post.images.thumb ?: SiteImage.placeholderTh.url
    override val light get() = rgb(240, 100, 180 )
    override val geoPoint get() = post.event.geoPoint
    override val markerType get() = MarkerType.Event
}

data class GalaxyMarker(
    val galaxy: Galaxy
): FeatureMarker {
    override val thumbUrl get() = galaxy.images.thumb ?: SiteImage.placeholderTh.url
    override val geoPoint get() = galaxy.geoPoint
    override val markerId get() = galaxy.galaxyId.string
    override val markerType get() = MarkerType.Galaxy
}