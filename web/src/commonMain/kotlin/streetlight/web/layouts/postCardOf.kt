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
        galaxyName = post.galaxyName,
        galaxySlug = post.galaxySlug,
        username = post.username,
        heading = post.label,
        subHeading = post.subtitle,
        postRoute = post.route,
        subRoute = post.subRoute,
        imageUrl = post.images.medium,
        description = post.description,
        colorScheme = post.colorScheme,
        links = post.links,
        isLit = post.isLit,
        lightCount = post.lightCount,
        postedAt = post.createdAt,
        details = post.cells
    )
}

// previews

fun FlowContent.postCardOf(location: Location) {
    feedPost(
        postId = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = location.label,
        subHeading = location.sublabel,
        postRoute = null, subRoute = null,
        imageUrl = location.images.medium,
        description = location.description,
        colorScheme = ColorScheme.Primary,
        links = location.links,
        details = locationCells(location)
    )
}

fun FlowContent.postCardOf(edit: LocationEdit, username: String?) {
    feedPost(
        postId = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = edit.label,
        subHeading = edit.subLabel,
        postRoute = null, subRoute = null,
        imageUrl = edit.imageRef,
        description = edit.description,
        colorScheme = ColorScheme.Primary,
        links = edit.links,
        details = locationCells(username, edit)
    )
}

fun FlowContent.postCardOf(event: EventLocation) {
    feedPost(
        postId = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = event.label,
        subHeading = event.locationLabel,
        postRoute = null, subRoute = null,
        imageUrl = event.images.medium,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        links = event.links,
        details = eventCells(event)
    )
}

fun FlowContent.postCardOf(event: EventEdit, location: Location) {
    feedPost(
        postId = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = event.title ?: "[Title]",
        subHeading = "${location.name ?: location.address ?: "[Location]"}, ${location.city ?: "[City]"}",
        postRoute = null, subRoute = null,
        imageUrl = event.imageRef,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        links = event.displayedLinks,
        details = eventCells(event)
    )
}