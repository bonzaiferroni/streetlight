package streetlight.model.data

import kampfire.model.GeoPoint

/** Anything that places itself at a location: its name, address and point. */
sealed interface LocationEntity {
    val locationId: LocationId?
    val name: String?
    val address: String?
    val city: String?
    val geoPoint: GeoPoint?
}