package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import koala.Svg
import koala.modifier.*
import kotlinx.css.px
import koala.html.*
import koala.markdown.MarkdownStyle
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kotlinx.css.pct
import streetlight.model.data.CuratorStatus
import streetlight.model.data.Entity
import streetlight.model.data.ExtraLink
import streetlight.model.ui.GalaxyRoute
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.CuratorMenu
import streetlight.web.ui.PopoverId
import streetlight.web.ui.curatorBadge
import kotlin.time.Clock

/**
 * An entity in a feed. Its layout follows the page's [FeedMode].
 *
 * A [curator] replaces the flair badge with its marks.
 */
fun FlowContent.feedRow(
    entity: Entity,
    curator: CuratorStatus? = null,
    cells: List<EntityCell>? = entity.toCells(),
) {
    div() {
        configureFeedRow(entity, curator, cells)
    }
}

/** Renders a [feedRow] into this div. */
fun DIV.configureFeedRow(
    entity: Entity,
    curator: CuratorStatus? = null,
    cells: List<EntityCell>? = entity.toCells(),
) {
    addModifiers(modify(FeedRowStyle.Base, ZenBg))

    val image = entity.image // td: make placeholder depend on post type
    val colorScheme = entity.toThemeColor()
    val flair = entity.toFlair()
    val postRoute = entity.toRoute()
    val headingUrl = entity.url?.value ?: postRoute?.toRelativePath()
    val heading = entity.label
    val buttons = entityButtonsOf(entity, true)
    val description = entity.description
    val links = entity.links

    entity.post?.postId?.let {
        setAttribute(AppAttribute.PostId.to(it))
    }
    curator?.let {
        setAttribute(CuratorMenu.CuratorJson.to(it))
    }

    // each child takes a grid area, placed by FeedMode
    div(FeedRowStyle.Content) {
        setStyle(Css.ColorScheme.of(colorScheme.cssValue))
        navigationIfNotNull(postRoute, modify(FeedRowStyle.Image, OverflowClip, MoonShadow)) {
            containImage(image, modify(FeedRowStyle.Feature, Size100P))
        }
        column(modify(FeedRowStyle.Text, Gap(0), TextShadow, OverflowHidden)) {
            navigationIfNotNull(headingUrl) {
                heading5(heading, modify(LineHeight115, SingleLine, Bold))
            }
            markdown(entity.body, modify(MarginTop(2.px), TextSmall, OpacityHigh, LineHeight115))
        }
        div(FeedRowStyle.Badge) {
            when (curator) {
                null -> flairBadge(flair.small)
                else -> curatorBadge(curator)
            }
        }
        cellGrid(cells, buttons, modify(FeedRowStyle.Cells, BorderRadius2, OverflowClip, Outline))
    }

    entityBody(description, links, limit = 1000)
}

/** The description and links of an entity, shared by [feedRow] and `entityHeader`, with an edit link to [editRoute]. */
fun FlowContent.entityBody(
    description: Markdown?,
    links: List<ExtraLink>?,
    editRoute: AppRoute? = null,
    limit: Int? = null,
) {
    if (description == null && links == null && editRoute == null) return
    div(modify(FeedRowStyle.ExpandedContent, Padding(2), Gap(2))) {
        description?.let {
            markdown(it, FeedRowStyle.ExpandedBody, limit = limit)
        }
        if (links != null || editRoute != null) {
            div(FeedRowStyle.ExpandedLinks) {
                links?.forEach { link ->
                    btn(link.label, link.url, Zen)
                }
                editRoute?.let {
                    btn("edit", it, Zen)
                }
            }
        }
    }
}

fun FlowContent.flairBadge(flair: Svg) {
    icon(flair, modify(FeedRowStyle.Flair, ColorSchemeFg, OpacityLow))
}

/** Who posted [entity] and when, and to which galaxy when [isUniverse]. */
fun FlowContent.postLine(entity: Entity, isUniverse: Boolean) {
    when (isUniverse) {
        true -> {
            column(modify(MarginTop(2.px), TextSmall, OpacityHigh, Gap(0))) {
                markdown(entity.body, modify(LineHeight115, FadeBottom))
            }
        }
        else -> {
            val username = entity.post?.username ?: entity.username
            val postedAt = entity.post?.createdAt ?: entity.createdAt ?: return

            column(modify(MarginTop(2.px), TextSmall, OpacityHigh, Gap(0))) {
                textBlock {
                    +"posted by "
                    when (username) {
                        null -> {
                            span("Someone", Bold)
                        }
                        else -> {
                            button {
                                setPopoverTarget(PopoverId.StarMenu)
                                setAttribute(Attribute.Username.to(username))
                                span(username.value, PrimaryFg)
                            }
                        }
                    }
                    +" "
                    span((Clock.System.now() - postedAt).toAgoFormat())
                }
            }
        }
    }
}

/** The layouts of a feed row, chosen by the viewer with a root switch. */
enum class FeedMode { Minimal, Row, Grid }

object FeedRowStyle {
    // a site-wide setting, held on the root element
    val Mode = enumAttributeOf<FeedMode>("feed-mode")
    val Feed = Class("feed")

    val Base = Class("feed-row")
    val Content = Base.withBemElement("content")
    val Image = Base.withBemElement("image")
    val Text = Base.withBemElement("text")
    val Badge = Base.withBemElement("badge")
    val Flair = Base.withBemElement("flair")
    val Feature = Base.withBemElement("feature")
    val MoreButton = Base.withBemElement("more-button")
    val ExpandedContent = Base.withBemElement("expanded-content")
    val ExpandedLinks = Base.withBemElement("expanded-links")
    val ExpandedBody = Base.withBemElement("expanded-body")
    val Cells = Base.withBemElement("cells")

    val ToggleExpand = Base.withBemModifier("expand-row")
    val Featured = Base.withBemModifier("featured")
}

val FeedRowCss get() = with(FeedRowStyle) {
    val grid = "${Mode.selector(FeedMode.Grid)} $Feed $Base, $Featured"
    val minimal = "${Mode.selector(FeedMode.Minimal)} $Feed $Base"
    //language="CSS"
    """
    
$Base {
    --row-height: calc(var(--unit) * 10);
    --text-inset: calc(var(--unit) / 2);
    display: grid;
    grid-template-rows: auto 1fr;
    container-type: inline-size;
    padding: 2px;

    &:not($ToggleExpand) {
        $ExpandedContent {
            display: none;
        }
    }
}

$Content {
    display: grid;
    grid-template-columns: auto 1fr auto;
    grid-template-areas:
        "image text badge"
        "cells cells cells";
    align-items: center;
    gap: var(--unit);

    @container (min-width: 960px) {
        grid-template-columns: auto 1fr auto calc(50% - var(--unit) / 2);
        grid-template-areas: "image text badge cells";
    }
}

$Image {
    grid-area: image;
    align-self: start;
    width: var(--row-height);
    aspect-ratio: 1;
    border: var(--outline-low);
    border-radius: var(--unit);
}

/* the fade has fixed stops, so only text that reaches the clip fades; it ends at the inset */
$Text {
    --clip: var(--row-height);
    --fade-end: calc(var(--clip) - var(--text-inset));
    grid-area: text;
    align-self: start;
    max-height: var(--clip);
    padding-block: var(--text-inset);
    mask-image: linear-gradient(to bottom, black calc(var(--fade-end) - 1rem), transparent var(--fade-end));
}

/* the body sets its blocks close and its headings at its size, a step heavier than its text */
$Text ${MarkdownStyle.Container} {
    :is(h1, h2, h3, h4, h5, h6) { font-size: inherit; font-weight: 500; }

    ${MarkdownStyle.Block} > * { margin-top: 0; }
}

/* the thumbnail is cut to match a cover fit, which hides the backdrop */
$Image $Feature { object-fit: cover; }

$Badge { grid-area: badge; }

/* a block, so the badge holds no line box below the icon */
$Flair { display: block; width: var(--row-height); }

$Cells { grid-area: cells; }

${Mode.selector(FeedMode.Grid)} $Feed {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));

    > :not($Base) { grid-column: 1 / -1; }
}

/* Grid and Minimal drop the border of Row */
$grid, $minimal {
    $Image { border: none; }
}

/* a featured entry takes the Grid layout in any feed */
$grid {
    --text-inset: 0px;
    padding: 0 0 var(--unit);

    /* the zero-width outer columns and the gap inset the text and cells from the edges */
    $Content {
        grid-template-columns: 0 1fr auto 0;
        grid-template-areas:
            "image image image image"
            ". text badge ."
            ". cells cells .";
    }

    $Image {
        width: auto;
        aspect-ratio: 3 / 2;
        border-radius: 0;
    }

    $Image $Feature { object-fit: contain; }

    $ExpandedContent, $MoreButton { display: none; }
}

$Featured $Text { --clip: calc(var(--unit) * 24); }

$minimal {
    --row-height: calc(var(--unit) * 8);

    $Content {
        grid-template-columns: auto 1fr auto;
        grid-template-areas: "image text badge";
    }

    $Cells, $ExpandedContent, $MoreButton { display: none; }
}

$ExpandedContent {
    overflow: hidden;
    display: grid;
    grid-template-rows: min-content auto;
    grid-template-areas: "links" "body";
    
    $ExpandedLinks {
        grid-area: links;
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(var(--unit-16), 1fr));
        justify-items: center;
        align-content: start;
        gap: var(--unit);

        > * {
            width: 100%;
            max-width: var(--unit-32);
        }
    }
    $ExpandedBody  { grid-area: body }
    
    @container (min-width: 960px) { 
        grid-template-rows: none;
        grid-template-columns: 1fr min-content;
        grid-template-areas: "body links";

        $ExpandedLinks { grid-template-columns: max-content; }
    }
}

"""}