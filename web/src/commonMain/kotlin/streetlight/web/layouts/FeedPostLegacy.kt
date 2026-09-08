package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import kabinet.utils.toMetricString
import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.utils.takeEllipsis
import koala.Image
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.model.data.LightType
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.StarRoute
import kotlin.time.Clock
import kotlin.time.Instant

@Deprecated("use feedPost")
fun FlowContent.feedPostLegacy(
    postSlug: Slug?,
    username: Username?,
    galaxyName: String?,
    galaxySlug: Slug?,
    heading: String,
    subHeading: String?,
    postRoute: AppRoute?,
    subRoute: AppRoute?,
    image: Image?,
    description: Markdown?,
    isLit: Boolean = false,
    lightCount: Int = 0,
    postedAt: Instant? = null,
    colorScheme: ColorScheme = ColorScheme.Primary,
    links: List<ExtraLink>?,
    details: (FlowContent.() -> Unit)?,
) {
    div(modify(FeedPostLegacy.Class)) {
        setStyle(Property.ColorScheme.to(colorScheme.cssValue))
        if (postSlug == null) addModifiers(FeedPostLegacy.HideLight)

        postSlug?.let {
            setAttribute(FeedKey.Attribute.to(it))

            // boost
            column(modify(GridArea.Light, LightControl.Class, LightControl.getLitMod(isLit), Gap0)) {
                setAttribute(LightControl.TypeData.to(LightType.Post))

                box(modify(Aspect1, AlignItemsCenter, BorderRadius50P, BorderSolid2Px, MarginTop1)) {
                    textBlock(
                        lightCount.toMetricString(),
                        mod = modify(LightControl.Counter, TextAlignCenter, TextSmall, LineHeight1)
                    )
                }

                box(modify(OpacityHigh)) {
//                    postId.let {
//                        onClick = LightControl.ToggleFun.invoke(ThisElement, postId)
//                    }
                    icon(SvgFile.Boost, modify(LightControl.UnlitIcon))
                    icon(SvgFile.BoostFilled, modify(LightControl.LitIcon))
                }
            }
        }

        // image
        navigationIfNotNull(postRoute, modify(GridArea.Image)) {
            featureImage(image, modify(Size100P, MinHeight0))
        }

        // body
        column(modify(GridArea.Body)) {

            // headings
            div(modify(Gap0, Flex1)) {
                // details
                if (details != null) {
                    cellBlock(modify(FeedPostLegacy.Details, FlexWrap), details)
                }

                navigationIfNotNull(postRoute, modify(AlignSelfStart)) {
                    heading4(heading.takeEllipsis(60), modify(LineHeight115, Shrinkable))
                }
                subHeading?.let {
                    navigationIfNotNull(subRoute, modify(AlignSelfStart)) {
                        textBlock(subHeading, modify(OpacityHigh, TextSmall))
                    }
                }
                spacer(modify(Height2Px, InkGradientBg, MarginTopTiny))
                row(modify(MarginTopTiny, AlignItemsCenter)) {
                    textBlock(mod = modify(TextSmall)) {
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
                                navigation(StarRoute(username)) {
                                    span("$username ")
                                }
                            }
                        }
                        postedAt?.let {
                            span((Clock.System.now() - postedAt).toAgoFormat())
                        }
                    }
//                    postSlug?.let { slug ->
//                        postMenu(slug, username)
////                        val anchor = PositionAnchor("menu-${postId}")
////                        icon(SvgFile.Dots, modify(Height3)) {
////                            setAnchorName(anchor)
////                            setPopoverTarget(PostKey.PostMenuId)
////                            onClick = KoalaFun.CallMenu.invoke(anchor, PostKey.PostMenuId, postId)
////                        }
//                    }
                }
            }
        }

        row(modify(GridArea.Content, MarginBottom2)) {
            description?.let {
                card(modify(PaperGradientBg, Padding2, Flex1)) {
                    markdown(it, limit = 1000)
                }
            }
            links?.let { links ->
                row(modify(FlexWrap, AlignItemsStart)) {
                    links.forEach { link ->
                        btn(link.label, link.url.value, modify(Zen))
                    }
                }
            }
        }
    }
}

object FeedPostLegacy {
    // val SmallRow = Class("small-row")
    val LargeRow = Class("large-row")
    val GridCard = Class("grid-card")
    val ToggleExpand = Class("expand-post")
    val HideLight = Class("hide-light")
    val Details = Class("details")

    val Class = Class("post-grid")
    val Controls = Class("controls")
    val Links = Class("links")
    val FeedColumn = Class("feed-column")

    val MinifiedWidth = 700
}

object GridArea {
    val Body = Class("body")
    val Image = Class("image")
    val Light = Class("boost")
    val Content = Class("content")
}

//language=CSS
val FeedPostCss get() = with(FeedPostLegacy) { """
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
    > ${GridArea.Content} { grid-area: content; overflow: hidden; }   
}

$Class {
    grid-template-columns: 2rem 5rem 1fr;
    grid-template-rows: auto 0fr;
    grid-template-areas: 
        "boost image body"
        "content content content";
    
    > ${GridArea.Image} {
        box-shadow: var(--moon-shadow);
        height: 5rem;
    }
    
    > ${GridArea.Image}, $Details {
        border-radius: var(--unit-spacing);
        overflow: clip;
    }
    
    &$HideLight {
        grid-template-columns: 0 5rem 1fr;
    }
    
    $Details {
        float: right;
        width: 8rem;
        margin-left: 1rem;
    }
    
    @container (min-width: ${MinifiedWidth}px) {
        $Details {
            width: 16rem;
        }
        
        &$HideLight {
            grid-template-columns: 0 5rem 1fr;
        }
    }
    
    @container (max-width: ${MinifiedWidth}px) {
        $Details {
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