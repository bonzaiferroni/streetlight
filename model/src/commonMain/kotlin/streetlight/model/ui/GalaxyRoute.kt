package streetlight.model.ui

import kampfire.api.Slug

data class GalaxyRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.Galaxy
    override val title get() = "Galaxy"
    override val label get() = "Feed"
}

object GalaxyFoundryRoute: StreetlightRoute {
    override val screen get() = Screen.GalaxyFoundry
    override val title get() = "Create Galaxy"
}

data class GalaxyConfigRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.GalaxyConfig
    override val title get() = "Galaxy Config"
    override val label get() = "Dash"
}

object GalaxyListRoute: StreetlightRoute {
    override val screen get() = Screen.GalaxyList
    override val title get() = "Galaxies"
}
