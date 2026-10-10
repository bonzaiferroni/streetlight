package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.createDiv
import koala.html.AppRoute
import kotlinx.coroutines.flow.drop
import streetlight.model.data.EntityFeed
import streetlight.model.data.FeedType
import streetlight.model.ui.FeedRoute
import streetlight.web.layouts.feedSection
import web.dom.document

/**
 * Replaces the page's feed section in place whenever the route changes its feed, with the feed [readFeed] reads for
 * the new route, its types linking through [typeRoute]. [onFeed] receives each feed shown. A read that gives `null`,
 * having delivered its problem, leaves the section as it is.
 */
inline fun <reified Route : FeedRoute> ViewScope.followFeedRoute(
    noinline typeRoute: (FeedType) -> AppRoute,
    noinline onFeed: (EntityFeed) -> Unit = {},
    noinline readFeed: suspend (Route) -> EntityFeed?,
) {
    launchEffect {
        portal.routeState.flow.drop(1).collect { route ->
            val feedRoute = route as? Route ?: return@collect
            val feed = readFeed(feedRoute) ?: return@collect
            val section = document.querySelector(AppAttribute.FeedSource.selector) ?: return@collect
            val freshSection = document.createDiv { feedSection(feed, typeRoute) }.firstElementChild ?: return@collect
            section.replaceWith(freshSection)
            onFeed(feed)
        }
    }
}
