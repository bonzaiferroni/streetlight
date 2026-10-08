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

/** The menu of tags a feed can be filtered by; a tag replaces the feed with its events that carry it. */
fun ViewScope.wireTagFilterMenu() {
    popoverMenu(TagFilterMenu.PopoverId, {
        TagFilterArgs(
            source = it.getClosestAttribute(AppAttribute.FeedSource) ?: return@popoverMenu null,
            galaxyId = it.getClosestAttribute(AppAttribute.GalaxyId),
            cityId = it.getClosestAttribute(AppAttribute.CityId),
        )
    }) { (source, galaxyId, cityId) ->
        row(modify(FlexWrap, Gap(1))) {
            EventTag.entries.forEach { tag ->
                button(tag.label, mod = Zen, onClick = {
                    document.requireElement(TagFilterMenu.PopoverId).closePopover()
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
    val source: FeedSource,
    val galaxyId: GalaxyId?,
    val cityId: CityId?,
)
