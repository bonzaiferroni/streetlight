package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

// gtfs data csv columns
// stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station,stop_timezone,wheelchair_boarding
// 26199,26199,Nine Mile Station Gate J,Vehicles Travelling Southwest,39.657746,-104.846604,,,0,33719,,1
@Serializable
data class TransitStop(
    val transitStopId: TransitStopId,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
) {
    companion object {
        fun fromCsv(csv: List<String>) = TransitStop(
            transitStopId = TransitStopId(csv[0]),
            name = csv[2],
            latitude = csv[4].toDouble(),
            longitude = csv[5].toDouble(),
            description = csv[3].ifBlank { null }
        )
    }
}

@JvmInline
@Serializable
value class TransitStopId(val value: String)

// route_id,service_id,trip_id,trip_headsign,direction_id,block_id,shape_id
// 0,SA,115699058,Union Station,0,   0  4,1325342
data class TransitTrip(
    val transitTripId: String,
    val transitRouteId: TransitRouteId,
    val serviceId: String,
    val tripId: String,
    val tripHeadsign: String,
    val directionId: Int,
    val blockId: String,
    val shapeId: String,
) {
    companion object {
        fun fromCsv(csv: List<String>) = TransitTrip(
            transitTripId = csv[2],
            transitRouteId = TransitRouteId(csv[0]),
            serviceId = csv[1],
            tripId = csv[2],
            tripHeadsign = csv[3],
            directionId = csv[4].toInt(),
            blockId = csv[5],
            shapeId = csv[6],
        )
    }
}

// trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled,timepoint
// 115699058,11:34:00,11:34:00,26175,1,,0,1,,1
data class TransitStopTime(
    val tripId: String,
    val arrivalTime: String,
    val departureTime: String,
    val transitStopId: TransitStopId,
    val stopSequence: Int,
) {
    companion object {
        fun fromCsv(csv: List<String>) = TransitStopTime(
            tripId = csv[0],
            arrivalTime = csv[1],
            departureTime = csv[2],
            transitStopId = TransitStopId(csv[3]),
            stopSequence = csv[4].toInt(),
        )
    }
}

// for each transit route, create a set of transit trips, and from that create a set of stopIds you can associate with the route