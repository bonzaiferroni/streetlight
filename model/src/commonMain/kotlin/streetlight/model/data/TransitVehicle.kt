package streetlight.model.data

import kampfire.model.GeoPoint
import kotlinx.serialization.Serializable

/** A vehicle's live position on its trip. */
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

/** Where a vehicle is relative to its next stop. */
enum class StopStatus {
    IncomingAt,
    StoppedAt,
    InTransitTo,
}

/** The live vehicles of an area. */
@Serializable
data class AreaTransitState(
    val timestamp: Long,
    val vehicles: List<TransitVehicle>,
)