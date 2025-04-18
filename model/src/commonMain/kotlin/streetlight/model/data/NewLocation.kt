package streetlight.model.data

import kabinet.model.GeoPoint

data class NewLocation(
    val areaId: Int,
    val geoPoint: GeoPoint,
)