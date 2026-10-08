package streetlight.web.ui

import kampfire.model.toDataOr
import koala.dom.*
import koala.modifier.*
import streetlight.model.data.CityId
import streetlight.model.data.EntityCursor
import streetlight.model.data.EventTag
import streetlight.model.data.FeedSource
import streetlight.model.data.GalaxyId
import streetlight.web.interop.appendFeed
import streetlight.web.interop.readFeed
import streetlight.web.layouts.FeedSection
import web.dom.document
import web.html.HTMLElement

/**
 * The menu of tags a feed can be filtered by. A tag replaces the feed with its events that carry it, and labels the
 * button that opened the menu and the heading of its feed.
 */
fun ViewScope.wireTagFilterMenu() {
    popoverMenu(TagFilterMenu.PopoverId, {
        TagFilterArgs(
            invoker = it,
            source = it.getClosestAttribute(AppAttribute.FeedSource) ?: return@popoverMenu null,
            galaxyId = it.getClosestAttribute(AppAttribute.GalaxyId),
            cityId = it.getClosestAttribute(AppAttribute.CityId),
        )
    }) { (invoker, source, galaxyId, cityId) ->
        row(modify(FlexWrap, Gap(1))) {
            EventTag.entries.forEach { tag ->
                button(tag.label, mod = Zen, onClick = {
                    document.requireElement(TagFilterMenu.PopoverId).closePopover()
                    invoker.setAttribute(TagFilterMenu.Tag, tag.label)
                    invoker.closest(AppAttribute.FeedSource.selector)?.querySelector(FeedSection.Heading.selector)
                        ?.setAttribute(TagFilterMenu.Tag, tag.label)
                    val mount = document.requireElement(FeedSection.MountId)
                    launchEffect("filter feed by tag") {
                        mount.modify(OpacityHigh)
                        val cursor = EntityCursor.Upcoming.copy(tag = tag.ordinal)
                        val feed = readFeed(source, galaxyId, cityId, cursor).toDataOr(toaster) {
                            mount.unmodify(OpacityHigh)
                            return@launchEffect
                        }
                        mount.unmodify(OpacityHigh)
                        mount.clear()
                        mount.append {
                            appendFeed(feed)
                        }
                    }
                })
            }
        }
    }
}

private data class TagFilterArgs(
    val invoker: HTMLElement,
    val source: FeedSource,
    val galaxyId: GalaxyId?,
    val cityId: CityId?,
)
