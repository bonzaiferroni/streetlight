package streetlight.model.ui

import kampfire.api.Slug

data class MediaRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.Media
    override val title get() = "Media"
}

data class MediaForgeRoute(override val slug: Slug?): SlugRoute {
    override val screen get() = Screen.MediaForge
    override val title get() = "Media Forge"
}

data class MediaUpdateRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.MediaUpdate
    override val title get() = "Media Update"
}
