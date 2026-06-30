package streetlight.web.layouts

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.model.Url
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import kotlin.time.Instant

fun FlowContent.feedPostProto(
    postSlug: Slug?,
    username: Username?,
    galaxyName: String?,
    galaxySlug: Slug?,
    heading: String,
    postRoute: AppRoute?,
    imageUrl: Url?,
    description: Markdown?,
    postedAt: Instant? = null,
    colorScheme: ColorScheme = ColorScheme.Primary,
    links: List<ExtraLink>?,
    details: (FlowContent.() -> Unit)?,
) {
    div(modify(FeedProto.Base)) {
        div(modify(FeedProto.Content)) {
            setStyle(Property.ColorScheme.to(colorScheme.cssValue))
            row(modify(Height10)) {
                navigationIfNotNull(postRoute, modify(Width10, OverflowClip, BorderRadius50P, BorderSolid2Px, MoonShadow)) {
                    image(imageUrl, modify(Size100P, ObjectFitCover))
                }
                column(modify(Gap0, JustifyContentCenter, Flex1)) {
                    heading4(heading, modify(LineHeight115, Shrinkable, LineClamp2, TextOverflowEllipses))
                    spacer(modify(Height2Px, InkGradientBg, MarginTopTiny))
                    postInfo(galaxySlug, galaxyName, username, postedAt)
                }
            }
            // spacer(modify(Height2Px, InkGradientBg, MarginTop2Px))
            details?.let {
                cellBlock(modify(FeedPost.Details, BorderRadius2, OverflowClip), details)
            }
        }

        row(modify(FeedProto.ExpandedContent, MarginBottom2)) {
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

object FeedProto {
    val Base = Class("feed-proto")
    val Content = Class("feed-proto__content")
    val ExpandedContent = Class("feed-proto__expanded-content")

    val ToggleExpand = Class("expand-post")
    val MinifiedWidth = 800
}

//language="CSS"
val FeedProtoCss get() = with(FeedProto) { """
    
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