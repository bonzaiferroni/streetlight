package streetlight.web.layouts

import kampfire.api.Markdown
import kampfire.model.Url
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.model.data.Post

fun FlowContent.feedPost(
    post: Post?,
    isGalaxyContext: Boolean,
    heading: String,
    postRoute: AppRoute?,
    imageUrl: Url?,
    description: Markdown?,
    colorScheme: ColorScheme = ColorScheme.Primary,
    links: List<ExtraLink>?,
    cells: (FlowContent.() -> Unit)?,
) {
    div(modify(FeedPost.Base)) {
        div(modify(FeedPost.Content)) {
            setStyle(Property.ColorScheme.to(colorScheme.cssValue))
            row(modify(Height10)) {
                navigationIfNotNull(postRoute, modify(Width10, OverflowClip, BorderRadius50P, BorderSolid2Px, MoonShadow)) {
                    image(imageUrl, modify(Size100P, ObjectFitCover))
                }
                column(modify(Gap0, JustifyContentCenter, Flex1)) {
                    navigationIfNotNull(postRoute) {
                        heading5(heading, modify(LineHeight115, Shrinkable, LineClamp2, TextOverflowEllipses))
                    }
                    spacer(modify(Height2Px, InkGradientBg, MarginTopTiny))
                    post?.let {
                        postInfo(post, isGalaxyContext)
                    }
                }
            }
            // spacer(modify(Height2Px, InkGradientBg, MarginTop2Px))
            cells?.let {
                cellBlock(modify(FeedPostLegacy.Details, BorderRadius2, OverflowClip), cells)
            }
        }

        row(modify(FeedPost.ExpandedContent, MarginBottom2)) {
            description?.let {
                card(modify(PaperGradientBg, Padding2, Flex1)) {
                    markdown(it, limit = 1000)
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
    }
}

object FeedPost {
    val Base = Class("feed-post")
    val Content = Class("feed-post__content")
    val ExpandedContent = Class("feed-post__expanded-content")

    val ToggleExpand = Class("expand-post")
}

//language="CSS"
val FeedProtoCss get() = with(FeedPost) { """
    
$Base {
    display: grid;
    gap: 0;
    grid-template-rows: auto 0fr;
    
    &$ToggleExpand {
        grid-template-rows: auto 1fr;
        gap: var(--unit-spacing);
    }
    
    &$Transitioning {
        transition: grid-template-rows var(--magic-interval) var(--magic-easing);
    }
}

$Content {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    align-items: center;
    gap: var(--unit-spacing);
    align-self: start;
}

$ExpandedContent {
    overflow: hidden;
}

"""}