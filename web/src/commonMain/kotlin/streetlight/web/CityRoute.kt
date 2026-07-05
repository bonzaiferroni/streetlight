package streetlight.web

import kampfire.api.Slug
import streetlight.model.data.CityId

data class CityRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.City
    override val title get() = "City"
    override val label get() = "Feed"
}