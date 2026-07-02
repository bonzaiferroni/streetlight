package streetlight.web

import kampfire.api.Slug

data class MediumRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.Medium
    override val title get() = "Media"
}

data class MediumForgeRoute(override val slug: Slug?): SlugRoute {
    override val screen get() = StreetlightScreen.MediumForge
    override val title get() = "Media Forge"
}

data class MediumUpdateRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.MediaUpdate
    override val title get() = "Media Update"
}
