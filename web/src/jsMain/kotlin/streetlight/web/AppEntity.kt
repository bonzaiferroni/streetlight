package streetlight.web

import kampfire.model.GeoPoint
import streetlight.model.data.EventId
import streetlight.model.data.EventInfo
import streetlight.model.data.EventType
import streetlight.model.data.VehicleType

data class EventEntity(
    val info: EventInfo
): MarkerEntity {
    override val position get() = info.geoPoint
    override val entityId get() = info.eventId.value
    override val iconPath get() = when(info.eventType) {
        EventType.Show -> SvgPath.guitar
        EventType.Food -> SvgPath.food
        EventType.Fellowship -> SvgPath.fellowship
    }
}

data class TransitEntity(
    val vehicleId: String,
    override val position: GeoPoint,
    val vehicleType: VehicleType,
    override val opacity: Float,
    override val bearing: Float?,
): MarkerEntity {
    override val entityId get() = vehicleId
    override val iconPath get() = when (vehicleType) {
        VehicleType.Bus -> SvgPath.bus
        VehicleType.LightRail -> SvgPath.train
        VehicleType.Train -> SvgPath.train
    }
}

fun VehiclePosition.toEntity(currentTime: Long, vehicleType: VehicleType): TransitEntity? {
    val vehicleId = vehicle?.id ?: return null
    val position = position ?: return null
    val vehicleTime = timestamp.toString().toLong()
    val secondsSinceCapture = (currentTime - vehicleTime).toInt()
    val opacity = (1 - secondsSinceCapture / 240f).coerceIn(.5f, 1f)
    return TransitEntity(
        vehicleId = vehicleId,
        position = position.toGeoPoint(),
        vehicleType = vehicleType,
        opacity = opacity,
        bearing = position.bearing
    )
}