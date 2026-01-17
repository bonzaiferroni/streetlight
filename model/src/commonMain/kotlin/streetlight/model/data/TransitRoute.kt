package streetlight.model.data

import kabinet.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Ahoy! This be the TransitRoute model, representin' the paths our vessels take across the sea or land.
 */
@Serializable
data class TransitRoute(
    val transitRouteId: TransitRouteId,
    val shortName: String,
    val longName: String,
    val description: String?,
    val vehicleType: VehicleType?,
)

/**
 * The unique identifier for a noble transit route.
 */
@JvmInline
@Serializable
value class TransitRouteId(val value: String)

enum class VehicleType {
    Bus,
    LightRail,
    Train,
}

// gtfs data csv columns
// route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color
// 0,RTD,0,Broadway,This Route Travels Northbound & Southbound,3,http://www.rtd-denver.com/Schedules.shtml,0076CE,FFFFFF