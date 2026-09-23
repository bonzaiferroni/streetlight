package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import koala.Svg
import koala.modifier.*
import kotlinx.css.px
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kampfire.api.Markdown
import streetlight.model.data.CuratorStatus
import streetlight.model.data.Entity
import streetlight.model.data.ExtraLink
import streetlight.model.ui.GalaxyRoute
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.CuratorMenu
import streetlight.web.ui.PopoverId
import streetlight.web.ui.curatorBadge
import kotlin.time.Clock

fun FlowContent.feedRow(
    entity: Entity,
    isUniverse: Boolean,
    curator: CuratorStatus? = null,
    cells: List<EntityCell>? = entity.toCells(),
) {
    div() {
        configureFeedRow(entity, isUniverse, curator, cells)
    }
}

fun DIV.configureFeedRow(
    entity: Entity,
    isUniverse: Boolean,
    curator: CuratorStatus? = null,
    cells: List<EntityCell>? = entity.toCells(),
) {
    addModifiers(modify(FeedRow.Base, ZenBg))

    val featureImage = entity.image // td: make placeholder depend on post type
    val colorScheme = entity.toThemeColor()
    val flair = entity.toFlair()
    val postRoute = entity.toRoute()
    val heading = entity.label
    val buttons = entityButtonsOf(entity, true)
    val description = entity.body
    val links = entity.links

    entity.post?.postId?.let {
        setAttribute(AppAttribute.PostId.to(it))
    }
    curator?.let {
        setAttribute(CuratorMenu.CuratorJson.to(it))
    }

    // each child takes a grid area, placed by FeedMode
    div(FeedRow.Content) {
        setStyle(Css.ColorScheme.of(colorScheme.cssValue))
        navigationIfNotNull(postRoute, modify(FeedRow.Image, OverflowClip, MoonShadow)) {
            image(featureImage, modify(Size100P, ObjectFitCover))
        }
        column(modify(FeedRow.Text, Gap(0), JustifyContentCenter, TextShadow)) {
            navigationIfNotNull(postRoute) {
                heading5(heading, modify(LineHeight115, Shrinkable, LineClamp2, TextOverflowEllipses))
            }
            postLine(entity, isUniverse)
        }
        div(FeedRow.Badge) {
            when (curator) {
                null -> flairBadge(flair.small)
                else -> curatorBadge(curator)
            }
        }
        cellGrid(cells, buttons, modify(FeedRow.Cells, BorderRadius2, OverflowClip, Outline))
    }

    entityBody(description, links, limit = 1000)
}

// the description and links of an entity, shared by feedRow and entityHeader
fun FlowContent.entityBody(
    description: Markdown?,
    links: List<ExtraLink>?,
    editRoute: AppRoute? = null,
    limit: Int? = null,
) {
    if (description == null && links == null && editRoute == null) return
    div(modify(FeedRow.ExpandedContent, Padding(2), Gap(2))) {
        description?.let {
            markdown(it, FeedRow.ExpandedBody, limit = limit)
        }
        if (links != null || editRoute != null) {
            div(FeedRow.ExpandedLinks) {
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
    icon(flair, modify(Width(10), ColorSchemeFg, OpacityLow))
}

fun FlowContent.postLine(entity: Entity, isUniverse: Boolean) {
    val username = entity.post?.username ?: entity.username ?: return
    val postedAt = entity.post?.createdAt ?: entity.createdAt ?: return
    val galaxy = entity.post?.galaxy?.takeIf { isUniverse }

    column(modify(MarginTop(2.px), TextSmall, Gap(0), OpacityHigh)) {
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
        galaxy?.let {
            textBlock {
                +"to "
                navigation(GalaxyRoute(it.slug)) {
                    span(it.name)
                }
            }
        }
    }
}

enum class FeedMode { Minimal, Row, Grid }

object FeedRow {
    val Mode = enumAttributeOf<FeedMode>("feed-mode")

    val Base = Class("feed-row")
    val Content = Base.withBemElement("content")
    val Image = Base.withBemElement("image")
    val Text = Base.withBemElement("text")
    val Badge = Base.withBemElement("badge")
    val MoreButton = Base.withBemElement("more-button")
    val ExpandedContent = Base.withBemElement("expanded-content")
    val ExpandedLinks = Base.withBemElement("expanded-links")
    val ExpandedBody = Base.withBemElement("expanded-body")
    val Cells = Base.withBemElement("cells")

    val ToggleExpand = Base.withBemModifier("expand-row")
}

//language="CSS"
val FeedProtoCss get() = with(FeedRow) { """
    
$Base {
    display: grid;
    gap: 0;
    grid-template-rows: auto 1fr;
    container-type: inline-size;
    padding: var(--unit);
    
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
    align-self: start;
    
    @container (min-width: 960px) {
        grid-template-columns: auto 1fr auto calc(50% - var(--unit) / 2);
        grid-template-areas: "image text badge cells";
    }
}

$Image {
    grid-area: image;
    width: calc(var(--unit) * 10);
    height: calc(var(--unit) * 10);
    border: var(--outline-low);
    border-radius: var(--unit);
}

$Text {
    grid-area: text;
    text-align: center;
}

$Badge { grid-area: badge; }

$Cells { grid-area: cells; }

${Mode.selector(FeedMode.Grid)} {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));

    > :not($Base) { grid-column: 1 / -1; }

    $Base { padding: 0 0 var(--unit); }

    $Content {
        grid-template-columns: 0 1fr auto 0;
        grid-template-areas:
            "image image image image"
            ". text badge ."
            ". cells cells .";
    }

    $Image {
        width: auto;
        height: auto;
        aspect-ratio: 3 / 2;
        border: none;
        border-radius: 0;
    }

    $Text { text-align: start; }

    $ExpandedContent, $MoreButton { display: none; }
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