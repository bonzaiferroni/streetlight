package streetlight.web

import koala.html.SiteImage
import koala.model.PointEntity
import streetlight.model.data.EventInfo
import streetlight.model.data.EventType

data class EventEntity(
    val info: EventInfo
): PointEntity {
    override val label get() = info.title
    override val position get() = info.geoPoint
    override val entityId get() = info.eventId.value
    override val thumbPath get() = info.thumbUrl ?: SiteImage.placeholderThumb
    override val isPrimary get() = true
//    override val iconPath get() = when(info.eventType) {
//        EventType.Show -> SvgPath.guitar
//        EventType.Food -> SvgPath.food
//        EventType.Meet -> SvgPath.meet
//    }
}