package streetlight.model.ui

import kampfire.api.Slug
import koala.html.AppRoute

data class CityRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.City
    override val title get() = "City"
}

data class CityConfigRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.CityConfig
    override val title get() = "City Config"
}

object CityListRoute: StreetlightRoute {
    override val screen get() = Screen.CityList
    override val title get() = "Cities"
}