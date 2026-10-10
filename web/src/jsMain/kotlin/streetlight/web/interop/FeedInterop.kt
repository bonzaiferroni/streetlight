package streetlight.web.interop

import kampfire.model.toDataOr
import koala.modifier.OpacityHigh
import koala.dom.AppendScope
import koala.dom.AppFacade
import koala.dom.append
import koala.dom.button
import koala.dom.clear
import koala.modifier.modify
import koala.modifier.requireAttribute
import koala.modifier.requireClosestAttribute
import koala.modifier.unmodify
import koala.modifier.setAttribute
import koala.interop.ThisElement
import koala.modifier.Zen
import koala.modifier.getAttribute
import kotlinx.html.onClick
import streetlight.model.data.EntityFeed
import streetlight.model.data.EntityCursor
import streetlight.model.data.EventTag
import streetlight.model.data.FeedRequest
import streetlight.model.data.FeedSource
import streetlight.web.layouts.FeedSection
import streetlight.web.layouts.toCells
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.RouteView
import streetlight.web.ui.TagFilterMenu
import streetlight.web.ui.api
import streetlight.web.ui.feedRow
import streetlight.web.ui.requireElement
import streetlight.web.ui.toaster
import web.dom.document
import web.html.HTMLElement
import web.html.HTMLInputElement
import web.dom.Element
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

/** Replaces the feed with its posts sorted by the mark on [element]. */
fun sortByMark(element: HTMLElement) {
    val markId = element.requireAttribute(AppAttribute.MarkId)
    val source = element.requireClosestAttribute(AppAttribute.FeedSource)
    val mount = document.requireElement(FeedSection.MountId)
    RouteView.activeScope.launchEffect {
        mount.modify(OpacityHigh)
        val feed = readFeed(source, EntityCursor.Mark(markId)).toDataOr(toaster) {
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
    val source = element.requireClosestAttribute(AppAttribute.FeedSource)
    val mount = document.requireElement(FeedSection.MountId)
    RouteView.activeScope.launchEffect {
        element.modify(OpacityHigh)
        val feed = readFeed(source, nextCursor).toDataOr(toaster) {
            element.unmodify(OpacityHigh)
            return@launchEffect
        }
        element.remove()
        mount.append {
            appendFeed(feed)
        }
    }
}

/** Labels the tag button of the feed holding [element] with [tag], or clears it, and reads the feed again. */
fun filterFeedByTag(element: HTMLElement, tag: EventTag?) {
    val section = element.feedSection()
    section.querySelector(TagFilterMenu.Button.selector)?.let { button ->
        when (tag) {
            null -> button.removeAttribute(TagFilterMenu.Tag.identifier)
            else -> button.setAttribute(TagFilterMenu.Tag, tag.label)
        }
    }
    refreshFeed(section)
}

/** Clears the tag filter of the feed holding [element]. */
fun clearFeedTag(element: HTMLElement) = filterFeedByTag(element, null)

/** Reads the feed holding the search field [element] again once its text has rested for [SEARCH_DEBOUNCE]. */
fun searchFeed(element: HTMLElement) = refreshFeed(element.feedSection(), SEARCH_DEBOUNCE)

/**
 * Replaces the rows of the feed [section] with its first page, filtered by the tag on its tag button and the text
 * of its search field, after [wait]. A newer read cancels one still waiting or loading.
 */
private fun refreshFeed(section: Element, wait: Duration = Duration.ZERO) {
    val source = section.requireAttribute(AppAttribute.FeedSource)
    val tagLabel = section.querySelector(TagFilterMenu.Button.selector)?.getAttribute(TagFilterMenu.Tag)
    val tag = EventTag.entries.firstOrNull { it.label == tagLabel }
    val search = (section.querySelector(FeedSection.Search.selector) as? HTMLInputElement)?.value
        ?.trim()?.takeIf { it.isNotEmpty() }
    val mount = document.requireElement(FeedSection.MountId)
    refreshJob?.cancel()
    refreshJob = RouteView.activeScope.launchEffect("refresh feed") {
        delay(wait)
        mount.modify(OpacityHigh)
        val cursor = EntityCursor.Upcoming.copy(tag = tag?.ordinal, search = search)
        val feed = readFeed(source, cursor).toDataOr(toaster) {
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

private var refreshJob: Job? = null

private val SEARCH_DEBOUNCE = 500.milliseconds

private fun Element.feedSection() = closest(AppAttribute.FeedSource.selector) ?: error("element is not in a feed section")

/** The page at [cursor] of the feed from [source]. */
suspend fun AppFacade.readFeed(source: FeedSource, cursor: EntityCursor) = api.feed.readFeed(FeedRequest(source, cursor))

/** Appends the rows of [feed], and a more button when it continues. */
fun AppendScope.appendFeed(feed: EntityFeed) {
    feed.entities.forEach { entity ->
        feedRow(entity, feed.curatorOf(entity), entity.toCells(feed.source?.context))
    }
    feed.nextCursor?.let {
        button("more", mod = Zen) {
            setAttribute(FeedSection.NextCursor.to(it))
            onClick = FeedSection.MorePosts.invokeJs(ThisElement)
        }
    }
}