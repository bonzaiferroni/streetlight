package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import kabinet.utils.toMetricString
import kampfire.api.Slug
import kampfire.model.Url
import kampfire.utils.takeEllipsis
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.ExtraLink
import streetlight.model.data.LightType
import streetlight.model.data.PostId
import streetlight.web.GalaxyRoute
import streetlight.web.StarRoute
import kotlin.time.Clock
import kotlin.time.Instant

fun FlowContent.feedPost(
    postId: PostId?,
    username: String?,
    galaxyName: String?,
    galaxySlug: Slug?,
    heading: String?,
    subHeading: String?,
    postRoute: AppRoute?,
    subRoute: AppRoute?,
    imageUrl: Url?,
    description: String?,
    isLit: Boolean = false,
    lightCount: Int = 0,
    postedAt: Instant = Clock.System.now(),
    colorScheme: ColorScheme = ColorScheme.Primary,
    links: List<ExtraLink>?,
    details: (FlowContent.() -> Unit)?,
) {
    div(modify(FeedPost.Class)) {
        postId?.let {
            setAttribute(PostKey.Attribute.to(postId))
        }
        setStyle(Property.ColorScheme.to(colorScheme.cssValue))

        // boost
        column(modify(GridArea.Light, LightControl.Class, LightControl.getLitMod(isLit), Gap0)) {
            setAttribute(LightControl.TypeData.to(LightType.Post))

            box(modify(Aspect1, AlignItemsCenter, BorderRadius50P, BorderSolid2Px, MarginTop1)) {
                textBlock(
                    lightCount.toMetricString(),
                    mod = modify(LightControl.Counter, TextAlignCenter, SmallText, LineHeight1)
                )
            }

            box(modify(OpacityHigh)) {
                postId?.let {
                    onClick = LightControl.ToggleFun.invoke(ThisElement, postId)
                }
                icon(SvgFile.Boost, modify(LightControl.UnlitIcon))
                icon(SvgFile.BoostFilled, modify(LightControl.LitIcon))
            }
        }

        // image
        navigationIfNotNull(postRoute, modify(GridArea.Image)) {
            featureImage(imageUrl, modify(Size100P, MinHeight0))
        }

        // body
        column(modify(GridArea.Body)) {

            // headings
            column(modify(Gap0, Flex1, MarginTop1)) {
                navigationIfNotNull(postRoute) {
                    heading3(heading, modify(LineHeight115, Bold))
                }
                subHeading?.let {
                    navigationIfNotNull(subRoute) {
                        textBlock(subHeading, modify(OpacityHigh))
                    }
                }
                spacer(modify(Height2Px, InkGradientBg, MarginTop2Px))
                row(modify(MarginTop2Px, AlignItemsCenter)) {
                    textBlock(mod = modify(SmallText)) {
                        galaxySlug?.let {
                            navigation(GalaxyRoute(it)) {
                                span("${galaxyName ?: "g/$it"} • ")
                            }
                        }
                        +"posted by "
                        when (username) {
                            null -> {
                                span("Someone ", modify(Bold))
                            }
                            else -> {
                                navigation(StarRoute(Slug("s/$username"))) {
                                    span("$username ")
                                }
                            }
                        }
                        span((Clock.System.now() - postedAt).toAgoFormat())
                    }
                    postId?.let { postId ->
                        val anchor = PositionAnchor("menu-${postId}")
                        icon(SvgFile.Dots, modify(Height3)) {
                            setAnchorName(anchor)
                            setPopoverTarget(PostKey.PostMenuId)
                            onClick = KoalaFun.CallMenu.invoke(anchor, PostKey.PostMenuId, postId)
                        }
                    }
                }
            }
        }

        row(modify(GridArea.Content, MarginBottom2)) {
            description?.let {
                card(modify(PaperGradientBg, Padding2, Flex1)) {
                    markdown(it.takeEllipsis(1000))
                }
            }
            links?.let { links ->
                row(modify(FlexWrap, AlignItemsStart)) {
                    links.forEach { link ->
                        btn(link.label, link.url, modify(Zen))
                    }
                }
            }
        }

        // details
        if (details != null) {
            cellBlock(modify(GridArea.Details, FlexWrap), details)
        }
    }
}

object FeedPost {
    val SmallRow = Class("small-row")
    val LargeRow = Class("large-row")
    val GridCard = Class("grid-card")
    val ToggleExpand = Class("expand-post")

    val Class = Class("post-grid")
    val Controls = Class("controls")
    val Links = Class("links")
    val FeedColumn = Class("feed-column")

    val MinifiedWidth = 700
}

object GridArea {
    val Body = Class("body")
    val Image = Class("image")
    val Details = Class("details")
    val Light = Class("boost")
    val Content = Class("content")
}

//language=CSS
val FeedPostCss get() = with(FeedPost) { """
$Class {
    container-type: inline-size;
    padding: 0;
    display: grid;
    align-items: stretch;
    justify-items: stretch;
    gap: 0 var(--unit-spacing);
    transition: grid-template-rows var(--magic-interval) var(--magic-easing);
    
    > ${GridArea.Light}   { grid-area: boost; }
    > ${GridArea.Image}   { grid-area: image; }
    > ${GridArea.Body}    { grid-area: body; }
    > ${GridArea.Details} { grid-area: details; }
    > ${GridArea.Content} { grid-area: content; overflow: hidden; }   
}

$SmallRow {
    $Class {
        grid-template-columns: 2.5rem 6rem 1fr 8rem;
        grid-template-rows: auto 0fr;
        grid-template-areas: 
            "boost image body details"
            "content content content content";
        
        > ${GridArea.Image} {
            box-shadow: var(--moon-shadow);
            height: 6rem;
        }
        
        > ${GridArea.Image}, > ${GridArea.Details} {
            border-radius: var(--unit-spacing);
            overflow: clip;
        }
        
        > ${GridArea.Details} {
            align-self: start;
        }
        
        @container (min-width: ${MinifiedWidth}px) {
            grid-template-columns: 2.5rem 6rem 1fr 16rem;
        }
        
        @container (max-width: ${MinifiedWidth}px) {
            > ${GridArea.Details} {
                > :not(:first-child):not(:last-child) {
                    display: none;
                }
            }
        }
    }
    
    $ToggleExpand$Class {
        grid-template-rows: auto 1fr;
        gap: var(--unit-spacing);
    }
}
    
$LargeRow {
    $FeedColumn {
        gap: var(--unit-spacing-2);
    }
    
    $Class {
        background: var(--zen-bg);
        box-shadow: var(--moon-shadow);
        border-radius: var(--unit-spacing-2);
        overflow: clip;
        
        grid-template-columns: 1fr 2fr;
        grid-template-rows: auto auto;
        grid-template-areas: 
            "image body"
            "details details";
        
        @container (min-width: 600px) {
            grid-template-columns: 1fr 2fr 1fr;
            grid-template-rows: auto;
            grid-template-areas: "image body details";
        }
        
        > ${GridArea.Body} {
            text-align: center;
        }
        
        > ${GridArea.Image} {
            height: 0;
            min-height: 100%;
        }
    }
}

$FeedColumn {
    gap: var(--unit-spacing);
    container-type: inline-size;
}

""" }

// flairIcon?.let {
//     icon(it.svg, modify(Height9, PositionAbsolute, Top0, Right0, OpacityGhost, ColorSchemeFg))
// }

// description
// navigationIfNotNull(postRoute, modify(Flex1, OverflowHidden, FadeBottom)) {
//     description?.let {
//         textBlock(it.stripMarkdown(400))
//     }
// }