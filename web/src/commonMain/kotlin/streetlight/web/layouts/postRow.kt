package streetlight.web.layouts

import kampfire.api.Markdown
import kampfire.api.Username
import koala.Image
import koala.html.AppRoute
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.CuratorStatus
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventLocation
import streetlight.model.data.ExtraLink
import streetlight.model.data.GalaxyPost
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Post
import streetlight.model.data.FeedEntity
import streetlight.model.data.FeedMark
import streetlight.model.data.PostMark
import streetlight.model.data.PostType

fun FlowContent.postRow(
    post: Post?,
    showGalaxy: Boolean,
    heading: String,
    postRoute: AppRoute?,
    image: Image?,
    description: Markdown?,
    postType: PostType?,
    curator: CuratorStatus?,
    links: List<ExtraLink>?,
    cells: (FlowContent.() -> Unit)?,
) = feedRow(
    heading = heading,
    postRoute = postRoute,
    image = image,
    description = description,
    postType = postType,
    links = links,
    curator = curator,
    cells = cells,
) {
    post?.let {
        postLine(post, showGalaxy)
    }
}

fun FlowContent.postRow(
    post: FeedEntity,
    showGalaxy: Boolean,
    curator: CuratorStatus?,
) = when (post) {
    is GalaxyPost -> postRow(post, showGalaxy, curator)
    else -> entityRow(post, true)
}

fun FlowContent.postRow(
    post: GalaxyPost,
    showGalaxy: Boolean,
    curator: CuratorStatus?,
) = postRow(
    post = post.base,
    showGalaxy = showGalaxy,
    heading = post.label,
    postRoute = post.route,
    image = post.image,
    description = post.body,
    postType = post.postType,
    curator = curator,
    links = post.links,
    cells = post.cellContent(true),
)

fun FlowContent.entityRow(entity: FeedEntity, showMore: Boolean = false) = feedRow(
    heading = entity.label,
    postRoute = entity.contentRoute,
    image = entity.image,
    description = entity.body,
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