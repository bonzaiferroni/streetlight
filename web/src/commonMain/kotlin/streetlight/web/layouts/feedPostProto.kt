package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.model.Url
import kampfire.utils.takeEllipsis
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.web.GalaxyRoute
import streetlight.web.StarRoute
import kotlin.time.Clock
import kotlin.time.Instant

fun FlowContent.feedPostProto(
    postSlug: Slug?,
    username: Username?,
    galaxyName: String?,
    galaxySlug: Slug?,
    heading: String,
    subHeading: String?,
    postRoute: AppRoute?,
    subRoute: AppRoute?,
    imageUrl: Url?,
    description: Markdown?,
    isLit: Boolean = false,
    lightCount: Int = 0,
    postedAt: Instant? = null,
    colorScheme: ColorScheme = ColorScheme.Primary,
    links: List<ExtraLink>?,
    details: (FlowContent.() -> Unit)?,
) {
    div(modify(FeedProto.Class)) {
        setStyle(Property.ColorScheme.to(colorScheme.cssValue))
        row(modify(Height10)) {
            navigationIfNotNull(postRoute, modify(Width10, OverflowClip, BorderRadius50P, BorderSolid2Px, MoonShadow)) {
                image(imageUrl, modify(Size100P, ObjectFitCover))
            }
            column(modify(Gap0, JustifyContentCenter, Flex1)) {
                heading4(heading, modify(LineHeight115, Shrinkable, LineClamp2, TextOverflowEllipses))
                spacer(modify(Height2Px, InkGradientBg, MarginTop2Px))
                postInfo(galaxySlug, galaxyName, username, postedAt)
            }
        }
        // spacer(modify(Height2Px, InkGradientBg, MarginTop2Px))
        details?.let {
            cellBlock(modify(FeedPost.Details, BorderRadius2, OverflowClip), details)
        }
    }
}

object FeedProto {
    val Class = Class("feed-proto")

    val MinifiedWidth = 800
}

//language="CSS"
val FeedProtoCss get() = with(FeedProto) { """

$Class {
    display: grid;
    transition: grid-template-rows var(--magic-interval) var(--magic-easing);
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    align-items: center;
    gap: var(--unit-spacing);
}

"""}