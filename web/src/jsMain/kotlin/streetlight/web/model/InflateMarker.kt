package streetlight.web.model

import kabinet.utils.toFutureFormat
import koala.SiteImage
import koala.SvgFile
import koala.model.IconMarker
import koala.model.ThumbMarker
import kotlinx.html.DIV
import streetlight.model.data.EventGroup
import streetlight.web.dom.component.configureEventMarkerBody
import streetlight.model.data.EntityType

interface InflateMarker {
    val group: EventGroup
}

data class LocationInflateMarker(
    override val group: EventGroup
): IconMarker, InflateMarker {
    override val markerId = group.locationId.toString()
    override val label get() = group.label
    override val geoPoint get() = group.geoPoint
    override val typeLabel get() = group.tag?.label ?: MarkerType.Event.label
    override val svg get() = SvgFile.MapPin.small
    override val themeColor get() = EntityType.Location.cssValue
    override val light get() = EntityType.Location.light
}

data class EventInflateMarker(
    override val group: EventGroup
): ThumbMarker, InflateMarker {
    override val markerId = group.locationId.toString()
    override val label get() = group.label
    override val sublabel get() = group.startsAt?.toFutureFormat()
    override val thumbUrl get() = group.image?.thumb ?: SiteImage.placeholderTh
    override val geoPoint get() = group.geoPoint
    override val typeLabel get() = group.tag?.label ?: MarkerType.Event.label
    override val themeColor get() = EntityType.Event.cssValue
    override val light get() = EntityType.Event.light
    override val configureBody: DIV.() -> Unit get() = { configureEventMarkerBody(this@EventInflateMarker, group.locationName) }
}