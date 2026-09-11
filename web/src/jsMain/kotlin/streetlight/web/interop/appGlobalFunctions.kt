package streetlight.web.interop

import kabinet.utils.toMetricString
import kampfire.model.toDataOr
import koala.core.queryFirstOrNull
import koala.css.OpacityHigh
import koala.dom.AppendScope
import koala.dom.ViewScope
import koala.dom.append
import koala.dom.button
import koala.dom.clear
import koala.dom.modify
import koala.dom.requireAttribute
import koala.dom.requireClosest
import koala.dom.requireClosestAttribute
import koala.dom.toggle
import koala.dom.unmodify
import koala.html.setAttribute
import koala.interop.KtFunction
import koala.interop.ThisElement
import kotlinx.html.onClick
import streetlight.model.data.EntityFeed
import streetlight.model.data.LightEdit
import streetlight.model.data.PostCursor
import streetlight.web.layouts.FeedSection
import streetlight.web.layouts.LightControl
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.api
import streetlight.web.ui.feedRow
import streetlight.web.ui.requireElement
import streetlight.web.ui.toaster
import web.dom.document
import web.html.HTMLElement
import kotlin.uuid.Uuid

fun ViewScope.appGlobalFunctions() = listOf(
    KtFunction(LightControl.ToggleFun, this::toggleLight),
    KtFunction(AppFun.UpdateMark, this::queryAndUpdateMark),
    KtFunction(FeedSection.SortByMark, this::sortByMark),
    KtFunction(FeedSection.MorePosts, this::morePosts),
)

fun ViewScope.toggleLight(element: HTMLElement, postId: String) {
    val uuid = Uuid.parseOrNull(postId) ?: error("uuid not found")
    val base = element.requireClosest(LightControl.Class)
    val lightType = base.requireAttribute(LightControl.TypeData)
    val counter = base.queryFirstOrNull(LightControl.Counter) ?: error("counter not found")
    val isLit = base.toggle(LightControl.Lit)
    counter.textContent?.toIntOrNull()?.let {
        val count = if (isLit) it + 1 else it - 1
        counter.textContent = count.toMetricString()
    }
    launchEffect {
        api.editStarLink(LightEdit(uuid, isLit, lightType))
    }
}

fun ViewScope.sortByMark(element: HTMLElement) {
    val markId = element.requireAttribute(AppAttribute.MarkId)
    val galaxyId = element.requireClosestAttribute(AppAttribute.GalaxyId)
    val mount = document.requireElement(FeedSection.MountId)
    launchEffect {
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

fun ViewScope.morePosts(element: HTMLElement) {
    val nextCursor = element.requireAttribute(FeedSection.NextCursor)
    val galaxyId = element.requireClosestAttribute(AppAttribute.GalaxyId)
    val mount = document.requireElement(FeedSection.MountId)
    launchEffect {
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