package streetlight.web

import kampfire.api.StringId

data class GalaxyRoute(override val value: StringId): SlugOrIdRoute {
    override val screen get() = StreetlightScreen.Galaxy
    override val title get() = "Galaxy"
}

object GalaxyFoundryRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.GalaxyFoundry
    override val title get() = "Create Galaxy"
}

data class GalaxyConfigRoute(override val value: StringId): SlugOrIdRoute {
    override val screen get() = StreetlightScreen.GalaxyConfig
    override val title get() = "Galaxy Settings"
}

object GalaxyListRoute: StreetlightRoute {
    override val screen get() = StreetlightScreen.GalaxyList
    override val title get() = "Galaxies"
}
