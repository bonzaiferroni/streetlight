package streetlight.web.layout

import koala.SvgFile
import koala.modifier.*
import koala.interop.JsSignature
import koala.interop.ThisElement
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import kotlinx.html.onInput
import streetlight.model.data.*
import streetlight.web.page.AppAttribute
import streetlight.web.component.tagFilterMenu
import koala.component.*
import koala.model.*
import koala.page.*
import streetlight.model.data.toDetails

/**
 * The feed section of a page: a type menu with the [FeedMode] switch, a tag filter and search field for an event feed,
 * the mark filters of a galaxy, and the feed with a button for more.
 *
 * The type menu names the types the feed's context offers, each a link to [typeRoute] when given. The feed's source
 * marks the selected type and is where the more button, the filters and the search read from.
 */
fun FlowContent.feedSection(
    feed: EntityFeed,
    typeRoute: ((FeedType) -> WebRoute)? = null,
) {
    val source = feed.source ?: error("a feed section shows a feed with a source")
    val types = feed.types ?: source.context.types

    section {
        setAttribute(AppAttribute.FeedSource.to(source))

        row(modify(AlignItemsCenter, BorderBottom2Px)) {
            div(modify(FeedSection.TypeMenu, Flex1)) {
                types.forEach { type ->
                    val current = Attribute.AriaCurrent.to("page").takeIf { type == source.type }
                    navigationIfNotNull(typeRoute?.invoke(type), modify(FeedSection.TypeOption, current)) { +type.label }
                }
            }
            rootSwitch(FeedRowStyle.Mode, OpacityHigh) { icon(it.toSvg()) }
        }

        feedSectionBody(feed)
    }
}

/**
 * The parts of a [feedSection] below its type menu, which change with the type: the filters and search, and the feed
 * with a button for more.
 */
fun FlowContent.feedSectionBody(feed: EntityFeed) {
    val source = feed.source ?: error("a feed section shows a feed with a source")

    // td: filter and search posts
    if (source.type.isSearched) {
        row(modify(AlignItemsCenter, JustifyContentSpaceBetween)) {
            row(AlignItemsCenter) {
                if (source.type == FeedType.Events) tagFilterMenu()
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
        if (feed.entities.isEmpty()) emptyFeed()
        feed.entities.forEach { entity ->
            feedRow(entity, feed.curatorOf(entity), entity.toDetails(source.context))
        }

        feed.nextCursor?.let {
            button("more", modify(Zen)) {
                setAttribute(FeedSection.NextCursor.to(it))
                onClick = FeedSection.MorePosts.invokeJs(ThisElement)
            }
        }
    }
}

/** The message in place of a feed: an animated icon over the line [text], centered in its height. */
fun FlowContent.emptyFeed(text: String = FeedSection.EmptyText) = column(FeedSection.EmptyMod) { emptyFeedContent(text) }

/** The icon and [text] of [emptyFeed], shared with the feed's client-side append. */
fun DIV.emptyFeedContent(text: String = FeedSection.EmptyText) {
    icon(SvgFile.Binoculars.animated, Height(10))
    textBlock(text, OpacityHigh)
}

/** The column that feed rows mount in, and that more rows append to. */
fun FlowContent.layoutFeed(
    block: FlowContent.() -> Unit
) {
    column(FeedSection.MountId, FeedRowStyle.Feed) {
        block()
    }
}

object FeedSection {
    val EmptyMod = modify(AlignItemsCenter, JustifyContentCenter, Height(24))
    const val EmptyText = "Nothing to show!"
    const val FailedText = "We couldn't load this feed."
    val MountId = Id("feed-layout")
    val Search = Class("feed-search")
    val TypeMenu = Class("feed-type-menu")
    val TypeOption = Class("feed-type-option")

    val Attribute = slugAttributeOf("feed-slug")
    val FeedColumnMod = modify(Gap2Px, MoonShadow)
    val NextCursor = jsonAttributeOf<EntityCursor>("next-post-cursor")

    val SortByMark = JsSignature("sortByMark")
    val MorePosts = JsSignature("morePosts")
    val SearchFeed = JsSignature("searchFeed")
}

/** Whether a feed of this type has a search field. */
val FeedType.isSearched get() = when (this) {
    FeedType.Events, FeedType.StarGalaxies, FeedType.TopGalaxies, FeedType.NewGalaxies, FeedType.Cities, FeedType.Stars -> true
    FeedType.Locations, FeedType.GalaxyPosts, FeedType.Posts, FeedType.Media -> false
}

fun FeedMode.toSvg() = when (this) {
    FeedMode.Minimal -> SvgFile.LayoutMinimal.small
    FeedMode.Row -> SvgFile.LayoutRow.small
    FeedMode.Grid -> SvgFile.LayoutGrid.small
}

// language="CSS"
val FeedSectionCss get() = with(FeedSection) { """
${rootSwitchCss(FeedRowStyle.Mode, FeedMode.entries)}

/* the strut of a selected option sets the line box, so the baseline holds while the options change size */
$TypeMenu {
    font-size: var(--heading-2-size);
    line-height: 1;
}

$TypeOption {
    display: inline-block;
    font-size: var(--paragraph-size);
    font-weight: var(--btn-font-weight);
    opacity: var(--opacity-high);
    transition: font-size var(--magic-interval) var(--magic-easing), var(--transition-opacity);

    &[aria-current="page"] {
        font-size: var(--heading-2-size);
        opacity: 1;
    }

    & + & { margin-inline-start: var(--unit); }
}
""" }