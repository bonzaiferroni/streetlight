package streetlight.model.ui

import kampfire.api.Slug
import streetlight.model.data.FeedType

data class GalaxyRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.Galaxy
    override val title get() = "Galaxy"
}

object GalaxyFoundryRoute: StreetlightRoute {
    override val screen get() = Screen.GalaxyFoundry
    override val title get() = "Create Galaxy"
}

data class GalaxyConfigRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.GalaxyConfig
    override val title get() = "Galaxy Config"
}

data class GalaxyFeedRoute(override val feed: FeedType? = null): FeedRoute {
    override val screen get() = Screen.GalaxyFeed
    override val title get() = "Galaxies"
    override val defaultFeedRoute get() = copy(feed = null)
    override fun toRelativePath() = basePath.withFeedParam(feed)
}
