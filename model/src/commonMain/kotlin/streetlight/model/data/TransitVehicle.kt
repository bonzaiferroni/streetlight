package streetlight.model.data

import kampfire.model.GeoPoint
import kotlinx.serialization.Serializable

@Serializable
data class TransitVehicle(
    val vehicleId: TransitVehicleId,
    val routeId: String,
    val geoPoint: GeoPoint,
    val bearing: Float,
    val timestamp: Long,
//    val stopId: TransitStopId,
//    val stopStatus: StopStatus,
)

typealias TransitVehicleId = String

enum class StopStatus {
    IncomingAt,
    StoppedAt,
    InTransitTo,
}

@Serializable
data class AreaTransitState(
    val timestamp: Long,
    val vehicles: List<TransitVehicle>,
)