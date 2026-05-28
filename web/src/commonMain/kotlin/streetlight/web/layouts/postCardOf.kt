package streetlight.web.layouts

import kampfire.model.medium
import kotlinx.html.FlowContent
import streetlight.model.data.EventEdit
import streetlight.model.data.EventLocation
import streetlight.model.data.Location
import streetlight.model.data.GalaxyPost
import streetlight.model.data.LocationEdit

fun FlowContent.postCardOf(post: GalaxyPost) {
    feedPost(
        postId = post.postId,
        postSlug = post.slug,
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

    // row(modify(JustifyContentEnd, MarginRight2)) {
    //     textBlock(modifiers = modify(SmallText, LineHeight1, OpacityMost)) {
    //         span("— posted by ")
    //         span(post.username ?: "Someone", modify(Bold))
    //         span(" ${post.createdAt.toAgoFormat()}")
    //     }
    // }
}

fun FlowContent.postCardOf(location: Location) {
    feedPost(
        postId = null, postSlug = null,
        heading = location.label,
        subHeading = location.subLabel,
        postRoute = null, subRoute = null,
        imageUrl = location.images.medium,
        description = location.description,
        colorScheme = ColorScheme.Primary,
        flairIcon = FlairIcon.Location,
        links = location.links,
        cells = locationCells(location)
    )
}

fun FlowContent.postCardOf(edit: LocationEdit, username: String?) {
    feedPost(
        postId = null, postSlug = null,
        heading = edit.label,
        subHeading = edit.subLabel,
        postRoute = null, subRoute = null,
        imageUrl = edit.imageRef,
        description = edit.description,
        colorScheme = ColorScheme.Primary,
        flairIcon = FlairIcon.Location,
        links = edit.links,
        cells = locationCells(username, edit)
    )
}

fun FlowContent.postCardOf(event: EventLocation) {
    feedPost(
        postId = null, postSlug = null,
        heading = event.label,
        subHeading = event.locationLabel,
        postRoute = null, subRoute = null,
        imageUrl = event.images.medium,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        flairIcon = FlairIcon.Event,
        links = event.links,
        cells = eventCells(event)
    )
}

fun FlowContent.postCardOf(event: EventEdit, location: Location) {
    feedPost(
        postId = null, postSlug = null,
        heading = event.title ?: "[Title]",
        subHeading = "${location.name ?: location.address ?: "[Location]"}, ${location.city ?: "[City]"}",
        postRoute = null, subRoute = null,
        imageUrl = event.imageRef,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        flairIcon = FlairIcon.Event,
        links = event.displayedLinks,
        cells = eventCells(event)
    )
}