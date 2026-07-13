package streetlight.model.ui

import kampfire.api.Slug
import koala.html.AppRoute

data class CityRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.City
    override val title get() = "City"
    override val label get() = "Feed"
}

object CityListRoute: AppRoute {
    override val screen get() = Screen.CityList
    override val title get() = "Cities"
}