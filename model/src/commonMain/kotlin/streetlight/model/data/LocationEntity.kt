package streetlight.model.data

import kampfire.model.GeoPoint

sealed interface LocationEntity {
    val locationId: LocationId?
    val name: String?
    val address: String?
    val city: String?
    val geoPoint: GeoPoint?
}