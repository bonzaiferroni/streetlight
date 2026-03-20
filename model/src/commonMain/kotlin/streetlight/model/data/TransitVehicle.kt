package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class TransitVehicle(
    val vehicleId: TransitVehicleId,
    val label: String,
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