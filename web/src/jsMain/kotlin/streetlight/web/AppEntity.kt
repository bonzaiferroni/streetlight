package streetlight.web

import koala.dom.RenderContext
import koala.dom.box
import koala.html.SiteImage
import koala.model.PointEntity
import streetlight.model.data.EventInfo
import streetlight.model.data.EventType
import streetlight.web.shells.cardOf

data class EventEntity(
    val info: EventInfo
): PointEntity {
    override val label get() = info.title
    override val position get() = info.geoPoint
    override val entityId get() = info.eventId.value
    override val thumbPath get() = info.thumbUrl ?: SiteImage.placeholderThumb
    override val isPrimary get() = true
    override val focusCard: RenderContext.() -> Unit get() = {
        box {
            cardOf(info)
        }
    }
}