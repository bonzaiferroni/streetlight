package streetlight.web

import kampfire.api.Slug

data class MediaRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.Media
    override val title get() = "Media"
}

data class MediaForgeRoute(override val slug: Slug?): SlugRoute {
    override val screen get() = StreetlightScreen.MediaForge
    override val title get() = "Media Forge"
}

data class MediaUpdateRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.MediaUpdate
    override val title get() = "Media Update"
}
