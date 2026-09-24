package streetlight.web.interop

import kampfire.model.toDataOr
import koala.modifier.OpacityHigh
import koala.dom.AppendScope
import koala.dom.append
import koala.dom.button
import koala.dom.clear
import koala.modifier.modify
import koala.modifier.requireAttribute
import koala.modifier.requireClosestAttribute
import koala.modifier.unmodify
import koala.modifier.setAttribute
import koala.interop.ThisElement
import koala.modifier.getClosestAttribute
import kotlinx.html.onClick
import streetlight.model.data.EntityFeed
import streetlight.model.data.EntityCursor
import streetlight.web.layouts.FeedSection
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.RouteView
import streetlight.web.ui.api
import streetlight.web.ui.feedRow
import streetlight.web.ui.requireElement
import streetlight.web.ui.toaster
import web.dom.document
import web.html.HTMLElement

/** Replaces the feed with the galaxy's posts sorted by the mark on [element]. */
fun sortByMark(element: HTMLElement) {
    val markId = element.requireAttribute(AppAttribute.MarkId)
    val galaxyId = element.requireClosestAttribute(AppAttribute.GalaxyId)
    val mount = document.requireElement(FeedSection.MountId)
    RouteView.activeScope.launchEffect {
        mount.modify(OpacityHigh)
        val feed = api.post.readPosts(galaxyId, EntityCursor.Mark(markId)).toDataOr(toaster) {
            mount.unmodify(OpacityHigh)
            return@launchEffect
        }
        mount.unmodify(OpacityHigh)
        mount.clear()
        mount.append {
            appendFeed(feed)
        }
    }
}

/** Appends the next page of the feed and removes the more button [element]. */
fun morePosts(element: HTMLElement) {
    val nextCursor = element.requireAttribute(FeedSection.NextCursor)
    val galaxyId = element.getClosestAttribute(AppAttribute.GalaxyId)
    val cityId = element.getClosestAttribute(AppAttribute.CityId)
    val mount = document.requireElement(FeedSection.MountId)
    RouteView.activeScope.launchEffect {
        element.modify(OpacityHigh)
        val outcome = when (cityId) {
            null -> api.post.readPosts(galaxyId, nextCursor)
            else -> api.city.readCityFeed(cityId, nextCursor)
        }
        val feed = outcome.toDataOr(toaster) {
            element.unmodify(OpacityHigh)
            return@launchEffect
        }
        element.remove()
        mount.append {
            appendFeed(feed)
        }
    }
}

/** Appends the rows of [feed], and a more button when it continues. */
fun AppendScope.appendFeed(feed: EntityFeed) {
    feed.entities.forEach { post ->
        val curator = feed.curatorOf(post)
        feedRow(post, true, curator)
    }
    feed.nextCursor?.let {
        button("more") {
            setAttribute(FeedSection.NextCursor.to(it))
            onClick = FeedSection.MorePosts.invokeJs(ThisElement)
        }
    }
}