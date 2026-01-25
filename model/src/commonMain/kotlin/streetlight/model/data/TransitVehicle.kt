package streetlight.model.data

import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class TransitVehicle(
    val transitVehicleId: TransitVehicleId,
    val transitRouteId: TransitRouteId,
    val transitStopId: TransitStopId,
    val latitude: Double,
    val longitude: Double,
    val bearing: Int,
    val stopStatus: StopStatus,
)

@JvmInline
@Serializable
value class TransitVehicleId(override val value: String) : ProjectId {
    companion object {
        fun random() = TransitVehicleId(randomUuidString())
    }
}

enum class StopStatus {
    IncomingAt,
    StoppedAt,
    InTransitTo,
}