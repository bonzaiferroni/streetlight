package streetlight.web.interop

import kampfire.model.toDataOr
import koala.css.OpacityHigh
import koala.dom.AppendScope
import koala.dom.append
import koala.dom.button
import koala.dom.clear
import koala.dom.modify
import koala.dom.requireAttribute
import koala.dom.requireClosestAttribute
import koala.dom.unmodify
import koala.html.setAttribute
import koala.interop.ThisElement
import kotlinx.html.onClick
import streetlight.model.data.EntityFeed
import streetlight.model.data.PostCursor
import streetlight.web.interop.appendFeed
import streetlight.web.layouts.FeedSection
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.RouteView
import streetlight.web.ui.api
import streetlight.web.ui.feedRow
import streetlight.web.ui.requireElement
import streetlight.web.ui.toaster
import web.dom.document
import web.html.HTMLElement

fun sortByMark(element: HTMLElement) {
    val markId = element.requireAttribute(AppAttribute.MarkId)
    val galaxyId = element.requireClosestAttribute(AppAttribute.GalaxyId)
    val mount = document.requireElement(FeedSection.MountId)
    RouteView.activeScope.launchEffect {
        mount.modify(OpacityHigh)
        val feed = api.readPosts(galaxyId, PostCursor.Mark(markId)).toDataOr(toaster) {
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

fun morePosts(element: HTMLElement) {
    val nextCursor = element.requireAttribute(FeedSection.NextCursor)
    val galaxyId = element.requireClosestAttribute(AppAttribute.GalaxyId)
    val mount = document.requireElement(FeedSection.MountId)
    RouteView.activeScope.launchEffect {
        element.modify(OpacityHigh)
        val feed = api.readPosts(galaxyId, nextCursor).toDataOr(toaster) {
            element.unmodify(OpacityHigh)
            return@launchEffect
        }
        element.remove()
        mount.append {
            appendFeed(feed)
        }
    }
}

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