package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import koala.SiteImage
import koala.Svg
import koala.css.*
import koala.html.*
import kotlinx.css.GridTemplateColumns
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.CuratorStatus
import streetlight.model.data.FeedEntity
import streetlight.model.ui.GalaxyRoute
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.CuratorMenu
import streetlight.web.ui.PopoverId
import streetlight.web.ui.curatorBadge
import kotlin.time.Clock

fun FlowContent.feedRow(
    entity: FeedEntity,
    isUniverse: Boolean,
    curator: CuratorStatus? = null,
    cellContent: (FlowContent.() -> Unit)? = null
) {
    div() {
        configureFeedRow(entity, isUniverse, curator, cellContent)
    }
}

fun DIV.configureFeedRow(
    entity: FeedEntity,
    isUniverse: Boolean,
    curator: CuratorStatus? = null,
    cellContent: (FlowContent.() -> Unit)? = null
) {
    addModifiers(modify(FeedRow.Base, modify(Padding1, ZenBg)))

    val imageUrl = entity.image?.thumb ?: SiteImage.placeholder.thumb // td: make placeholder depend on post type
    val colorScheme = entity.colorScheme
    val flair = entity.flair
    val postRoute = entity.contentRoute
    val heading = entity.label
    val cells = cellContent ?: entity.getCells(true)
    val description = entity.body
    val links = entity.links

    entity.post?.postId?.let {
        setAttribute(AppAttribute.PostId.to(it))
    }
    curator?.let {
        setAttribute(CuratorMenu.CuratorJson.to(it))
    }

    div(modify(FeedRow.Content)) {
        setStyle(Property.ColorScheme.to(colorScheme.cssValue))
        row(modify(Height10)) {
            navigationIfNotNull(postRoute, modify(Width10, OverflowClip, BorderRadius1, BorderSolid2Px, MoonShadow)) {
                image(imageUrl, modify(Size100P, ObjectFitCover))
            }
            column(modify(Flex1, Gap0, JustifyContentCenter, AlignItemsCenter, TextShadow)) {
                navigationIfNotNull(postRoute) {
                    heading5(heading, modify(LineHeight115, Shrinkable, LineClamp2, TextOverflowEllipses, TextAlignCenter))
                }
                postLine(entity, isUniverse)
            }

            when (curator) {
                null -> flairBadge(flair.small)
                else -> curatorBadge(curator)
            }
        }
        // spacer(modify(Height2Px, InkGradientBg, MarginTop2Px))

        box(modify(AlignItemsCenter)) {
            cells?.let {
                cellBlock(modify(FeedRow.Cells, BorderRadius2, OverflowClip, Outline), cells)
            }
        }
    }

    grid(GridTemplateColumns("1fr min-content"), mod = modify(FeedRow.ExpandedContent, Padding2, Gap2)) {
        description?.let {
            markdown(it, modify(Flex1), limit = 1000)
        }
        links?.let { links ->
            row(modify(FlexWrap, AlignItemsStart, AlignContentStart)) {
                links.forEach { link ->
                    btn(link.label, link.url, modify(Zen))
                }
            }
        }
    }
}

fun FlowContent.flairBadge(flair: Svg) {
    icon(flair, modify(Width10, ColorSchemeFg, OpacityLow))
}

fun FlowContent.postLine(entity: FeedEntity, isUniverse: Boolean) {
    val username = entity.post?.username ?: entity.username ?: return
    val postedAt = entity.post?.createdAt ?: entity.createdAt ?: return
    val galaxy = entity.post?.galaxy?.takeIf { isUniverse }

    column(modify(MarginTopTiny, TextSmall, AlignItemsCenter, Gap0, OpacityHigh)) {
        textBlock {
            +"posted by "
            when (username) {
                null -> {
                    span("Someone", modify(Bold))
                }
                else -> {
                    button {
                        setPopoverTarget(PopoverId.StarMenu)
                        setAttribute(Attribute.Username.to(username))
                        span(username.value, modify(PrimaryFg))
                    }
//                    navigation(StarRoute(username)) {
//                        span("$username ")
//                    }
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

object FeedRow {
    val Base = Class("feed-row")
    val Content = Base.withBemElement("content")
    val ExpandedContent = Base.withBemElement("expanded-content")
    val Cells = Base.withBemElement("cells")

    val ToggleExpand = Base.withBemModifier("expand-row")
}

//language="CSS"
val FeedProtoCss get() = with(FeedRow) { """
    
$Base {
    display: grid;
    gap: 0;
    grid-template-rows: auto 1fr;
    
    &:not($ToggleExpand) {
        $ExpandedContent {
            display: none;
        }
    }
    
    &$Transitioning {
        transition: grid-template-rows var(--magic-interval) var(--magic-easing);
    }
}

$Content {
    display: flex;
    flex-direction: column;
    gap: var(--unit-spacing);
    align-self: start;
    
    @media (min-width: 960px) {
        flex-direction: row;
        
        > * {
            flex: 1;
        }
    }
}

$ExpandedContent {
    overflow: hidden;
}

"""}

//                 row(modify(Width10, FlexWrap, BorderRadius1, Gap2Px, OverflowClip, FlexItems1)) {
//                    menuCell(modify(MinWidth8)) {
//                        row(modify(AlignItemsCenter)) {
//                            icon(SvgFile.Star)
//                            textBlock("2")
//                        }
//                    }
//                    menuCell(modify(MinWidth4)) {
//                        icon(SvgFile.Info)
//                    }
//                    menuCell(modify(MinWidth4)) {
//                        icon(SvgFile.Dots)
//                    }
//                }