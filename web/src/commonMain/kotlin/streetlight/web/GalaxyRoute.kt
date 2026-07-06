package streetlight.web

import kampfire.api.Slug

data class GalaxyRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.Galaxy
    override val title get() = "Galaxy"
    override val label get() = "Feed"
}

object GalaxyFoundryRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.GalaxyFoundry
    override val title get() = "Create Galaxy"
}

data class GalaxyConfigRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.GalaxyUpdate
    override val title get() = "Galaxy Config"
    override val label get() = "Dash"
}

object GalaxyListRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.GalaxyList
    override val title get() = "Galaxies"
}
