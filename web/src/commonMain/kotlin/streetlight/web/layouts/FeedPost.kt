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
    flairIcon: FlairIcon?,
    links: List<ExtraLink>?,
    cellBlock: (FlowContent.() -> Unit)?,
) {
    row(modify(QueryContainer)) {
        column(modify(Width5)) {
            box(modify(Aspect1, AlignItemsCenter, BorderRadius50P, BorderSolid2Px, MarginTop1)) {
                textBlock("120k", modify(TextAlignCenter, SmallText, LineHeight1))
            }
            icon(SvgFile.Boost, modify(Height4, OpacityHalf))
        }

        div(modify(FeedPostMod.Grid, Flex1)) {
            postId?.let {
                setAttribute(PostKey.Attribute.to(postId))
            }
            setStyle(Property.ColorScheme.to(colorScheme.cssValue))

            // image
            navigationIfNotNull(postRoute, modify(GridArea.Image)) {
                featureImage(imageUrl, modify(Size100P, MinHeight0))
            }

            // body
            column(modify(GridArea.Body, Padding1)) {

                // headings
                column(modify(Gap0, Flex1)) {
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

                // controls
//                row(modify(modify(FeedPostMod.Controls, Height4, OpacityMost, JustifyContentCenter))) {
//                    icon(SvgFile.MapPin)
//                    icon(SvgFile.EyePlus)
//
//                }

                // links
                row(modify(FeedPostMod.Links, AlignItemsEnd)) {
                    row(modify(JustifyContentCenter, Flex1)) {
                        links?.forEach { link ->
                            btn(link.label, link.url, modify(Zen))
                        }
                    }
                }
            }

            // details
            if (cellBlock != null) {
                cellBlock(modify(GridArea.Details, FlexWrap), cellBlock)
            }
        }
    }
}

object FeedPostMod {
    val SmallRow = Class("small-row")
    val LargeRow = Class("large-row")
    val GridCard = Class("grid-card")

    val Grid = Class("post-grid")
    val Controls = Class("controls")
    val Links = Class("links")
    val FeedColumn = Class("feed-column")
}

object GridArea {
    val Body = Class("body")
    val Image = Class("image")
    val Details = Class("details")
    val Meta = Class("meta")
}

//language=CSS
val FeedPostCss get() = with(FeedPostMod) { """
$Grid {
    container-type: inline-size;
    padding: 0;
    display: grid;
    align-items: stretch;
    justify-items: stretch;
    gap: 0;
    
    > ${GridArea.Image}   { grid-area: image; }
    > ${GridArea.Body}    { grid-area: body; }
    > ${GridArea.Details} { grid-area: details; }
}

$SmallRow {
    $Grid {
        grid-template-columns: 6rem 1fr 6rem;
        grid-template-rows: auto;
        grid-template-areas: "image body details";
        
        > ${GridArea.Image} {
            box-shadow: var(--moon-shadow);
            height: 6rem;
        }
        
        > ${GridArea.Image}, > ${GridArea.Details} {
            border-radius: var(--unit-spacing);
            overflow: clip;
        }
        
        > ${GridArea.Details} {
            
            @container (max-width: 500px) {
                > :not(:first-child):not(:last-child) {
                    display: none;
                }
            }
        }
        
        $Links, $Controls {
            display: none;
        }
        
        @container (min-width: 500px) {
            grid-template-columns: 6rem 3fr 2fr;
            grid-template-rows: auto;
            grid-template-areas: "image body details";
        }
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