package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import kampfire.model.medium
import koala.css.Bold
import koala.css.JustifyContentEnd
import koala.css.LineHeight1
import koala.css.MarginRight2
import koala.css.OpacityMost
import koala.css.SmallText
import koala.css.modify
import koala.html.column
import koala.html.row
import koala.html.setAttribute
import koala.html.span
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.EventLocation
import streetlight.model.data.Location
import streetlight.model.data.GalaxyPost

fun FlowContent.postCardOf(post: GalaxyPost) {
    column {
        setAttribute(PostKey.Attribute.to(post.postId))

        postCard(
            postId = post.postId,
            heading = post.title,
            subHeading = post.subtitle,
            postRoute = post.route,
            subRoute = post.subRoute,
            imageUrl = post.images.medium,
            description = post.description,
            colorScheme = post.colorScheme,
            flairIcon = post.flairIcon,
            links = post.links,
            cells = post.cells
        )

        row(modify(JustifyContentEnd, MarginRight2)) {
            textBlock(modifiers = modify(SmallText, LineHeight1, OpacityMost)) {
                span("— posted by ")
                span(post.username ?: "Someone", modify(Bold))
                span(" ${post.createdAt.toAgoFormat()}")
            }
        }
    }
}

fun FlowContent.postCardOf(location: Location) {
    postCard(
        postId = null,
        heading = location.label,
        subHeading = location.subLabel,
        postRoute = null,
        subRoute = null,
        imageUrl = location.images.medium,
        description = location.description,
        colorScheme = ColorScheme.Primary,
        flairIcon = FlairIcon.Location,
        links = location.links,
        cells = locationCells(location)
    )
}

fun FlowContent.postCardOf(event: EventLocation) {
    postCard(
        postId = null,
        heading = event.label,
        subHeading = event.locationLabel,
        postRoute = null,
        subRoute = null,
        imageUrl = event.images.medium,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        flairIcon = FlairIcon.Event,
        links = event.links,
        cells = eventCells(event)
    )
}