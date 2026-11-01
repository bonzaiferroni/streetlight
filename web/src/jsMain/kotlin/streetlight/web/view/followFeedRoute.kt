package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.createDiv
import koala.core.queryAll
import kotlinx.coroutines.flow.drop
import streetlight.model.data.EntityFeed
import streetlight.model.ui.FeedRoute
import streetlight.web.layout.FeedSection
import streetlight.web.layout.emptyFeed
import streetlight.web.layout.feedSectionBody
import koala.modifier.Attribute
import koala.modifier.setAttribute
import js.array.asList
import web.dom.document
import streetlight.web.page.AppAttribute
import streetlight.web.model.portal

/**
 * Rebuilds the body of the page's feed section in place whenever the route changes its feed, with the feed [readFeed]
 * reads for the new route. The type menu stays, marking the new route's type before the read. [onFeed] receives each
 * feed shown. A read that gives `null`, having delivered its problem, shows the empty message below the menu.
 */
inline fun <reified Route : FeedRoute> ViewScope.followFeedRoute(
    noinline onFeed: (EntityFeed) -> Unit = {},
    noinline readFeed: suspend (Route) -> EntityFeed?,
) {
    launchEffect {
        portal.routeState.flow.drop(1).collect { route ->
            val feedRoute = route as? Route ?: return@collect
            val path = feedRoute.toRelativePath()
            document.queryAll(FeedSection.TypeOption).forEach { option ->
                if (option.getAttribute("href") == path) option.setAttribute(Attribute.AriaCurrent.to("page"))
                else option.removeAttribute(Attribute.AriaCurrent.identifier)
            }
            val feed = readFeed(feedRoute)
            val source = feed?.source
            val section = document.querySelector(AppAttribute.FeedSource.selector) ?: return@collect
            val body = document.createDiv {
                if (source == null) emptyFeed() else feedSectionBody(feed)
            }
            // the first child is the type menu's row
            while (section.childElementCount > 1) section.lastElementChild?.remove()
            source?.let { section.setAttribute(AppAttribute.FeedSource.to(it)) }
            section.append(*body.children.asList().toTypedArray())
            feed?.let(onFeed)
        }
    }
}
