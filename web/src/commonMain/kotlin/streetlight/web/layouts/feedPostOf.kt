package streetlight.web.layouts

import kampfire.api.Username
import kampfire.model.medium
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventLocation
import streetlight.model.data.Location
import streetlight.model.data.GalaxyPost
import streetlight.model.data.LocationEdit

fun FlowContent.feedPostOf(post: GalaxyPost) {
    feedPost(
        post = post.base,
        isGalaxyContext = true,
        heading = post.label,
        // subHeading = post.subtitle,
        postRoute = post.route,
        // subRoute = post.subRoute,
        imageUrl = post.images.medium,
        description = post.body,
        colorScheme = post.colorScheme,
        links = post.links,
        cells = post.cellContent(true),
        // isLit = post.isLit,
        // lightCount = post.lightCount,
    )
}

// previews

fun FlowContent.feedPostOf(location: Location) {
    feedPostLegacy(
        postSlug = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = location.label,
        subHeading = location.sublabel,
        postRoute = null, subRoute = null,
        imageUrl = location.images.medium,
        description = location.description,
        colorScheme = ColorScheme.Primary,
        links = location.links,
        details = cellContentOf(location)
    )
}

fun FlowContent.feedPostOf(edit: LocationEdit, username: Username?) {
    feedPostLegacy(
        postSlug = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = edit.label,
        subHeading = edit.subLabel,
        postRoute = null, subRoute = null,
        imageUrl = edit.imageRef,
        description = edit.description,
        colorScheme = ColorScheme.Primary,
        links = edit.links,
        details = cellContentOf(username, edit)
    )
}

fun FlowContent.feedPostOf(event: EventLocation) {
    feedPostLegacy(
        postSlug = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = event.label,
        subHeading = event.locationLabel,
        postRoute = null, subRoute = null,
        imageUrl = event.images.medium,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        links = event.links,
        details = cellContentOf(event, false, null)
    )
}

fun FlowContent.feedPostOf(event: Event) {
    feedPostLegacy(
        postSlug = null, username = event.scout,
        galaxyName = null, galaxySlug = null,
        heading = event.title,
        subHeading = null,
        postRoute = null, subRoute = null,
        imageUrl = event.images.medium,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        links = event.links,
        postedAt = event.createdAt,
        details = cellContentOf(event)
    )
}

fun FlowContent.feedPostOf(event: EventEdit, location: Location) {
    feedPostLegacy(
        postSlug = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = event.title ?: "[Title]",
        subHeading = "${location.name ?: location.address ?: "[Location]"}, ${location.city ?: "[City]"}",
        postRoute = null, subRoute = null,
        imageUrl = event.imageRef,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        links = event.displayedLinks,
        details = cellContentOf(event)
    )
}