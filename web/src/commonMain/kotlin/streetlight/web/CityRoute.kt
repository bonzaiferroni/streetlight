package streetlight.web

import kampfire.api.Slug
import koala.html.AppRoute
import koala.html.AppScreen
import streetlight.model.data.CityId

data class CityRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.City
    override val title get() = "City"
    override val label get() = "Feed"
}

object CityListRoute: AppRoute {
    override val screen get() = StreetlightScreen.CityList
    override val title get() = "Cities"
}