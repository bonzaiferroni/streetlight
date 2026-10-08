package streetlight.web.layouts

import koala.SvgFile
import koala.modifier.*
import koala.html.*
import koala.interop.JsSignature
import koala.interop.ThisElement
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import kotlinx.html.onInput
import streetlight.model.data.*
import streetlight.web.shells.SectionHeadingMod
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.tagFilterMenu

/**
 * The posts section of a page: a heading with the [FeedMode] switch, a tag filter and search field for an event feed,
 * the mark filters of a galaxy, and the feed with a button for more.
 *
 * The feed's source picks the heading and the reader of the more button, and [galaxyId] or [cityId] names the
 * feed it reads.
 */
fun FlowContent.feedSection(
    feed: EntityFeed,
    galaxyId: GalaxyId? = null,
    cityId: CityId? = null,
) {
    section {
        setAttribute(AppAttribute.FeedSource.to(feed.source))
        galaxyId?.let {
            setAttribute(AppAttribute.GalaxyId.to(it))
        }
        cityId?.let {
            setAttribute(AppAttribute.CityId.to(it))
        }

        // the spacer matches the switch, keeping the heading centered
        row(AlignItemsCenter) {
            div(FeedSection.SwitchWidth)
            filigree(Flex1) {
                heading2(feed.source.heading, SectionHeadingMod)
            }
            rootSwitch(FeedRowStyle.Mode, modify(FeedSection.SwitchWidth, JustifyContentEnd, OpacityHigh)) { icon(it.toSvg()) }
        }

        // td: filter and search posts
        if (feed.source != FeedSource.Posts) {
            row(modify(AlignItemsCenter, JustifyContentSpaceBetween)) {
                row(AlignItemsCenter) {
                    tagFilterMenu()
                }
                textField(placeholder = "search", textMod = FeedSection.Search) {
                    onInput = FeedSection.SearchFeed.invokeJs(ThisElement)
                }
            }
        }

        feed.marks?.takeIf { it.size == 1 }?.values?.first()?.let { feedMarks ->
            row(JustifyContentCenter) {
                feedMarks.forEach { mark ->
                    textBlock(mark.name) {
                        setAttribute(AppAttribute.MarkId.to(mark.markId))
                        onClick = FeedSection.SortByMark.invokeJs(ThisElement)
                    }
                }
            }
        }

        layoutFeed {
            // td: message when empty
            feed.entities.forEach { entity ->
                val curator = feed.curatorOf(entity)
                feedRow(entity, curator)
            }

            feed.nextCursor?.let {
                button("more", modify(Zen)) {
                    setAttribute(FeedSection.NextCursor.to(it))
                    onClick = FeedSection.MorePosts.invokeJs(ThisElement)
                }
            }
        }
    }
}

/** The column that feed rows mount in, and that more rows append to. */
fun FlowContent.layoutFeed(
    block: FlowContent.() -> Unit
) {
    column(FeedSection.MountId, modify(FeedRowStyle.Feed, FeedSection.FeedColumnMod)) {
        block()
    }
}

object FeedSection {
    val MountId = Id("feed-layout")
    val Search = Class("feed-search")

    val Attribute = slugAttributeOf("feed-slug")
    val FeedColumnMod = modify(Gap2Px, MoonShadow)
    val NextCursor = jsonAttributeOf<EntityCursor>("next-post-cursor")

    val SortByMark = JsSignature("sortByMark")
    val MorePosts = JsSignature("morePosts")
    val SearchFeed = JsSignature("searchFeed")

    val SwitchWidth = Width(12)
}

/** The heading of a feed from this source. */
val FeedSource.heading get() = when (this) {
    FeedSource.Events, FeedSource.City -> "Events"
    FeedSource.Posts -> "Posts"
}

fun FeedMode.toSvg() = when (this) {
    FeedMode.Minimal -> SvgFile.LayoutMinimal
    FeedMode.Row -> SvgFile.LayoutRow
    FeedMode.Grid -> SvgFile.LayoutGrid
}

// language="CSS"
val FeedSectionCss get() = """
${rootSwitchCss(FeedRowStyle.Mode, FeedMode.entries)}
"""