package streetlight.web.layouts

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.medium
import koala.Image
import koala.html.AppRoute
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventLocation
import streetlight.model.data.ExtraLink
import streetlight.model.data.GalaxyPost
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Post
import streetlight.model.data.Entity

fun FlowContent.postRow(
    post: Post?,
    isGalaxyContext: Boolean,
    heading: String,
    postRoute: AppRoute?,
    image: Image?,
    description: Markdown?,
    colorScheme: ColorScheme = ColorScheme.Primary,
    links: List<ExtraLink>?,
    cells: (FlowContent.() -> Unit)?,
) = feedRow(
    heading = heading,
    postRoute = postRoute,
    image = image,
    description = description,
    colorScheme = colorScheme,
    links = links,
    cells = cells,
) {
    post?.let {
        postInfo(post, isGalaxyContext)
    }
}

fun FlowContent.postRow(post: Entity) = when (post) {
    is GalaxyPost -> postRow(post)
    else -> entityRow(post, true)
}

fun FlowContent.postRow(post: GalaxyPost) = postRow(
    post = post.base,
    isGalaxyContext = true,
    heading = post.label,
    // subHeading = post.subtitle,
    postRoute = post.route,
    // subRoute = post.subRoute,
    image = post.image,
    description = post.body,
    colorScheme = post.colorScheme,
    links = post.links,
    cells = post.cellContent(true),
    // isLit = post.isLit,
    // lightCount = post.lightCount,
)

fun FlowContent.entityRow(entity: Entity, showMore: Boolean = false) = feedRow(
    heading = entity.label,
    postRoute = entity.contentRoute,
    image = entity.image,
    description = entity.body,
    colorScheme = entity.colorScheme,
    links = entity.links,
    cells = entity.getCells(showMore),
) {
    entity.sublabel?.let {
        textBlock(it)
    }
}

// previews

fun FlowContent.postRow(location: Location) {
    feedPostLegacy(
        postSlug = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = location.label,
        subHeading = location.sublabel,
        postRoute = null, subRoute = null,
        image = location.image,
        description = location.description,
        colorScheme = ColorScheme.Primary,
        links = location.links,
        details = cellContentOf(location)
    )
}

fun FlowContent.postRow(edit: LocationEdit, username: Username?) {
    feedPostLegacy(
        postSlug = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = edit.label,
        subHeading = edit.subLabel,
        postRoute = null, subRoute = null,
        image = edit.image,
        description = edit.description,
        colorScheme = ColorScheme.Primary,
        links = edit.links,
        details = cellContentOf(username, edit)
    )
}

fun FlowContent.postRow(event: EventLocation) {
    feedPostLegacy(
        postSlug = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = event.label,
        subHeading = event.locationLabel,
        postRoute = null, subRoute = null,
        image = event.image,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        links = event.links,
        details = cellContentOf(event, false, null)
    )
}

fun FlowContent.postRow(event: Event) {
    feedPostLegacy(
        postSlug = null, username = event.scout,
        galaxyName = null, galaxySlug = null,
        heading = event.title,
        subHeading = null,
        postRoute = null, subRoute = null,
        image = event.image,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        links = event.links,
        postedAt = event.createdAt,
        details = cellContentOf(event)
    )
}

fun FlowContent.postRow(event: EventEdit, location: Location) {
    feedPostLegacy(
        postSlug = null, username = null,
        galaxyName = null, galaxySlug = null,
        heading = event.title ?: "[Title]",
        subHeading = "${location.name ?: location.address ?: "[Location]"}, ${location.city ?: "[City]"}",
        postRoute = null, subRoute = null,
        image = event.image,
        description = event.description,
        colorScheme = ColorScheme.Accent,
        links = event.displayedLinks,
        details = cellContentOf(event)
    )
}