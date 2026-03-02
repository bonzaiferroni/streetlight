package streetlight.web.ui

import koala.dom.RenderContext
import koala.dom.box
import koala.html.SiteImage
import koala.model.PointEntity
import streetlight.model.data.Event
import streetlight.model.data.EventInfo
import streetlight.model.data.Location
import streetlight.web.shells.cardOf

data class ProtoEventEntity(
    val info: EventInfo
): PointEntity {
    override val label get() = info.title
    override val position get() = info.geoPoint
    override val entityId get() = info.eventId.value
    override val thumbPath get() = info.thumbUrl ?: SiteImage.placeholderThumb
    override val focusCard: RenderContext.() -> Unit get() = {
        box {
            cardOf(info)
        }
    }
}

data class LocationEntity(
    val location: Location
): PointEntity {
    override val label get() = location.name
    override val position get() = location.geoPoint
    override val entityId get() = location.locationId.value
    // override val thumbPath get() = info.thumbUrl ?: SiteImage.placeholderThumb
    override val focusCard: RenderContext.() -> Unit get() = {
        box {
            cardOf(location)
        }
    }
}

data class EventEntity(
    val location: Location,
    val events: List<Event>
): PointEntity {
    val event get() = events.first()
    override val label get() = location.name
    override val position get() = location.geoPoint
    override val entityId get() = location.locationId.value
    override val thumbPath get() = event.thumbUrl ?: SiteImage.placeholderThumb
    override val focusCard: RenderContext.() -> Unit get() = {
        box {
            cardOf(event)
        }
    }
}