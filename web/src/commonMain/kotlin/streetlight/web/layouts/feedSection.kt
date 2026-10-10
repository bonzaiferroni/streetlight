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
 * The feed section of a page: a heading with the [FeedMode] switch, a tag filter and search field for an event feed,
 * the mark filters of a galaxy, and the feed with a button for more.
 *
 * The feed's source picks the heading and is where the more button, the filters and the search read from. The
 * type menu left of the heading shows the types the feed's context offers, each a link to [typeRoute] when given.
 */
fun FlowContent.feedSection(
    feed: EntityFeed,
    typeRoute: ((FeedType) -> AppRoute)? = null,
) {
    val source = feed.source ?: error("a feed section shows a feed with a source")
    val types = feed.types ?: source.context.types

    section {
        setAttribute(AppAttribute.FeedSource.to(source))

        // the type menu matches the switch in width, keeping the heading centered
        row(AlignItemsCenter) {
            row(modify(FeedSection.SwitchWidth, OpacityHigh)) {
                types.forEach { type ->
                    val iconMod = modify(SmallIconHeight, PrimaryFg.takeIf { type == source.type })
                    when (typeRoute) {
                        null -> icon(type.toSvg(), iconMod)
                        else -> navigation(typeRoute(type)) { icon(type.toSvg(), iconMod) }
                    }
                }
            }
            filigree(Flex1) {
                heading2(source.type.heading, SectionHeadingMod)
            }
            rootSwitch(FeedRowStyle.Mode, modify(FeedSection.SwitchWidth, JustifyContentEnd, OpacityHigh)) { icon(it.toSvg()) }
        }

        // td: filter and search posts
        if (source.type == FeedType.Events) {
            row(modify(AlignItemsCenter, JustifyContentSpaceBetween)) {
                row(AlignItemsCenter) {
                    tagFilterMenu()
                }
                searchField(textMod = FeedSection.Search) {
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
                feedRow(entity, feed.curatorOf(entity), entity.toCells(source.context))
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

/** The heading of a feed of this type. */
val FeedType.heading get() = when (this) {
    FeedType.Events -> "Events"
    FeedType.Locations -> "Locations"
    FeedType.GalaxyPosts, FeedType.Posts -> "Posts"
    FeedType.Media -> "Media"
}

fun FeedType.toSvg() = when (this) {
    FeedType.Events -> SvgFile.Calendar.small
    FeedType.Locations -> SvgFile.MapPin.small
    FeedType.GalaxyPosts -> SvgFile.Planet.small
    FeedType.Posts -> SvgFile.News.small
    FeedType.Media -> SvgFile.Photo.small
}

fun FeedMode.toSvg() = when (this) {
    FeedMode.Minimal -> SvgFile.LayoutMinimal.small
    FeedMode.Row -> SvgFile.LayoutRow.small
    FeedMode.Grid -> SvgFile.LayoutGrid.small
}

// language="CSS"
val FeedSectionCss get() = """
${rootSwitchCss(FeedRowStyle.Mode, FeedMode.entries)}
"""