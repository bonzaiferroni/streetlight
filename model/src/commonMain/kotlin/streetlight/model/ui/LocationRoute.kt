package streetlight.model.ui

import kampfire.api.Slug
import streetlight.model.data.LocationId

data class LocationRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.Location
    override val title get() = "Location"
}

data class LocationUpdateRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.LocationUpdate
    override val title get() = "Share Location"
}

data class LocationScoutRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.LocationScout
    override val title get() = "Location Scout"
}

data class LocationConfigRoute(
    val locationId: LocationId
): StreetlightRoute, RecordIdRoute {
    override val screen get() = Screen.LocationAdmin
    override val recordId get() = locationId
    override val title get() = "Admin"
}