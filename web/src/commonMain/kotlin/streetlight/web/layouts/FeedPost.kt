package streetlight.web.layouts

import kampfire.api.Slug
import kampfire.model.Url
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.ExtraLink
import streetlight.model.data.PostId

fun FlowContent.feedPost(
    postId: PostId?,
    postSlug: Slug?,
    heading: String?,
    subHeading: String?,
    postRoute: AppRoute?,
    subRoute: AppRoute?,
    imageUrl: Url?,
    description: String?,
    colorScheme: ColorScheme,
    links: List<ExtraLink>?,
    details: (FlowContent.() -> Unit)?,
) {
    div(modify(FeedPostMod.Grid, FeedPostMod.ToggleExpand)) {
        postId?.let {
            setAttribute(PostKey.Attribute.to(postId))
        }
        setStyle(Property.ColorScheme.to(colorScheme.cssValue))

        // boost
        column(modify(GridArea.Boost)) {
            box(modify(Aspect1, AlignItemsCenter, BorderRadius50P, BorderSolid2Px, MarginTop1)) {
                textBlock("120k", modify(TextAlignCenter, SmallText, LineHeight1))
            }
            icon(SvgFile.Boost, modify(Height4, OpacityHalf))
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
                        textBlock(subHeading, modify(OpacityMost))
                    }
                }
                spacer(modify(Height2Px, InkGradientBg, MarginTop2Px))
                row(modify(MarginTop2Px, AlignItemsCenter, OpacityMost)) {
                    textBlock("posted by Luke 12 minutes ago", modify(SmallText))
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

        row(modify(GridArea.Content, MarginBottom2, MarginLeft1)) {
            description?.let {
                markdown(it, modify(Flex1))
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

object FeedPostMod {
    val SmallRow = Class("small-row")
    val LargeRow = Class("large-row")
    val GridCard = Class("grid-card")
    val ToggleExpand = Class("expand-post")

    val Grid = Class("post-grid")
    val Controls = Class("controls")
    val Links = Class("links")
    val FeedColumn = Class("feed-column")

    val MinifiedWidth = 700
}

object GridArea {
    val Body = Class("body")
    val Image = Class("image")
    val Details = Class("details")
    val Boost = Class("boost")
    val Content = Class("content")
}

//language=CSS
val FeedPostCss get() = with(FeedPostMod) { """
$Grid {
    container-type: inline-size;
    padding: 0;
    display: grid;
    align-items: stretch;
    justify-items: stretch;
    gap: 0 var(--unit-spacing);
    
    > ${GridArea.Boost}   { grid-area: boost; }
    > ${GridArea.Image}   { grid-area: image; }
    > ${GridArea.Body}    { grid-area: body; }
    > ${GridArea.Details} { grid-area: details; }
    > ${GridArea.Content} { 
        grid-area: content;
        max-height: 32rem;
        overflow: hidden;
    }   
}

$SmallRow {
    $Grid {
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
            grid-template-columns: 3rem 6rem 1fr 16rem;
        }
        
        @container (max-width: ${MinifiedWidth}px) {
            > ${GridArea.Details} {
                > :not(:first-child):not(:last-child) {
                    display: none;
                }
            }
        }
    }
    
    $ToggleExpand$Grid {
        grid-template-rows: auto 1fr;
        gap: var(--unit-spacing);
    }
}
    
$LargeRow {
    $FeedColumn {
        gap: var(--unit-spacing-2);
    }
    
    $Grid {
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