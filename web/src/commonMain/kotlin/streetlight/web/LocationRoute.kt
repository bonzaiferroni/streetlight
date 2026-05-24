package streetlight.web

import kampfire.api.Slug
import streetlight.model.data.LocationId

data class LocationRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.Location
    override val title get() = "Location"
}

data class UpdateLocationRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.LocationUpdate
    override val title get() = "Share Location"
}

data class LocationScoutRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.LocationScout
    override val title get() = "Location Scout"
}

data class LocationAdminRoute(
    val locationId: LocationId
): StreetlightRoute, RecordIdRoute {
    override val screen get() = StreetlightScreen.LocationAdmin
    override val recordId get() = locationId
    override val title get() = "Location Admin"
}