package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import kampfire.api.Markdown
import kampfire.api.Username
import koala.Image
import koala.SiteImage
import koala.Svg
import koala.css.*
import koala.html.*
import kotlinx.css.GridTemplateColumns
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.CuratorStatus
import streetlight.model.data.ExtraLink
import streetlight.model.data.GalaxyTrace
import streetlight.model.data.FeedMark
import streetlight.model.data.Post
import streetlight.model.data.PostId
import streetlight.model.data.PostMark
import streetlight.model.data.PostType
import streetlight.model.data.VoteType
import streetlight.model.data.getVoteType
import streetlight.model.ui.GalaxyRoute
import streetlight.web.ui.PopoverId
import streetlight.web.ui.curatorBadge
import streetlight.web.ui.multiBadge
import streetlight.web.ui.polarBadge
import streetlight.web.ui.singleBadge
import kotlin.time.Clock
import kotlin.time.Instant

fun FlowContent.feedRow(
    heading: String,
    postRoute: AppRoute?,
    image: Image?,
    description: Markdown?,
    postType: PostType? = null,
    curator: CuratorStatus? = null,
    links: List<ExtraLink>?,
    cells: (FlowContent.() -> Unit)?,
    subheading: (DIV.() -> Unit)?,
) {
    val imageUrl = image?.thumb ?: SiteImage.placeholder.thumb // td: make placeholder depend on post type
    val colorScheme = postType?.colorScheme ?: ColorScheme.Primary
    val flair = postType?.flair ?: FlairIcon.Default

    div(modify(FeedRow.Base, modify(Padding1, ZenBg))) {

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
//                    spacer(modify(ColorSchemeFg, Height2Px, InkGradientBg, MarginTopTiny, ParticleRay)) {
//                        setRandomSeed()
//                    }
                    subheading?.invoke(this)
                }

                when (curator) {
                    null -> flairBadge(flair.small)
                    else -> curatorBadge(curator)
                }
            }
            // spacer(modify(Height2Px, InkGradientBg, MarginTop2Px))

            cells?.let {
                cellBlock(modify(FeedRow.Cells, BorderRadius2, OverflowClip, Outline), cells)
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
}

fun FlowContent.flairBadge(flair: Svg) {
    icon(flair, modify(Width10, ColorSchemeFg, OpacityLow))
}

fun FlowContent.postLine(
    username: Username?,
    galaxy: GalaxyTrace?,
    postedAt: Instant
) {
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

fun FlowContent.postLine(
    post: Post,
    isGalaxyContent: Boolean
) = postLine(post.username, if (isGalaxyContent) post.galaxy else null, post.createdAt)

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
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(480px, 1fr));
    align-items: center;
    gap: var(--unit-spacing);
    align-self: start;
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