package streetlight.web.model

import koala.dom.RenderContext
import koala.dom.box
import koala.html.SiteImage
import koala.model.MapEntityId
import koala.model.PointEntity
import kotlinx.html.DIV
import kotlinx.html.p
import streetlight.model.data.Event
import streetlight.model.data.Location
import streetlight.model.data.Spirit
import streetlight.model.data.SpiritId
import streetlight.web.shells.cardOf

data class LocationEntity(
    val location: Location
): PointEntity {
    override val entityId get() = location.locationId.value
    override val label get() = location.name
    override val position get() = location.geoPoint
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
    override val entityId get() = location.locationId.value
    override val label get() = location.name
    override val position get() = location.geoPoint
    override val thumbPath get() = event.thumbUrl ?: SiteImage.placeholderThumb
    override val focusCard: RenderContext.() -> Unit get() = {
        box {
            cardOf(event)
        }
    }
}

data class SpiritEntity(
    val spirit: Spirit
): PointEntity {
    override val entityId get() = spirit.spiritId.toEntityId()
    override val label get() = spirit.name
    override val position get() = spirit.position
    override val body: DIV.() -> Unit get() = {
        p { +label }
    }
}

fun SpiritId.toEntityId(): MapEntityId = "spirit-${value}"