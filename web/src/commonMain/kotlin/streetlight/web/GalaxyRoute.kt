package streetlight.web

import kampfire.api.Slug

data class GalaxyRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.Galaxy
    override val title get() = "Galaxy"
}

object GalaxyFoundryRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.GalaxyFoundry
    override val title get() = "Create Galaxy"
}

data class GalaxyConfigRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.GalaxyUpdate
    override val title get() = "Galaxy Settings"
}

object GalaxyListRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.Galaxies
    override val title get() = "Galaxies"
}
