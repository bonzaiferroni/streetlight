package streetlight.model.ui

import kampfire.api.Slug
import streetlight.model.data.FeedType
import koala.model.AppRoute

data class CityRoute(override val slug: Slug, override val feed: FeedType? = null): SlugRoute, FeedRoute {
    override val screen get() = Screen.City
    override val title get() = "City"
    override val defaultFeedRoute get() = copy(feed = null)
    override fun toRelativePath() = toIdSitePath(slug).withFeedParam(feed)
}

data class CityConfigRoute(override val slug: Slug): SlugRoute {
    override val screen get() = Screen.CityConfig
    override val title get() = "City Config"
}

data class CityFeedRoute(override val feed: FeedType? = null): FeedRoute {
    override val screen get() = Screen.CityFeed
    override val title get() = "Cities"
    override val defaultFeedRoute get() = copy(feed = null)
    override fun toRelativePath() = basePath.withFeedParam(feed)
}