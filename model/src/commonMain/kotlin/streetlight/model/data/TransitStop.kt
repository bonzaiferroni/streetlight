package streetlight.model.data

import kabinet.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Ahoy! This here be the TransitStop model, a place where our noble vessels rest.
 */
@Serializable
data class TransitStop(
    val transitStopId: TransitStopId,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
)

/**
 * The unique identifier for a transit stop.
 */
@JvmInline
@Serializable
value class TransitStopId(val value: String)

// gtfs data csv columns
// stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station,stop_timezone,wheelchair_boarding
// 26199,26199,Nine Mile Station Gate J,Vehicles Travelling Southwest,39.657746,-104.846604,,,0,33719,,1