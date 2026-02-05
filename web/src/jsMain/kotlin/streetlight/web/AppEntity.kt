package streetlight.web

import kampfire.model.GeoPoint
import streetlight.model.data.EventInfo
import streetlight.model.data.EventType
import streetlight.model.data.VehicleType

data class EventEntity(
    val info: EventInfo
): PointEntity {
    override val position get() = info.geoPoint
    override val entityId get() = info.eventId.value
    override val iconPath get() = when(info.eventType) {
        EventType.Show -> SvgPath.guitar
        EventType.Food -> SvgPath.food
        EventType.Fellowship -> SvgPath.fellowship
    }
}