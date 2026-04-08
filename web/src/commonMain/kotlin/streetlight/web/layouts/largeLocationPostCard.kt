package streetlight.web.layouts

import kotlinx.html.FlowContent
import streetlight.model.data.LocationPost

fun FlowContent.largeLocationPostCard(post: LocationPost) {
    val location = post.location ?: return // td: show removed content

    largePostCard(
        title = post.title,
        subtitle = location.address,
        description = location.description,
        sourceUrl = location.website,
        links = null, // td: add location extra links
        images = location.images,
        postRoute = location.route,
        subRoute = null,
        cells = listOf() // td: add cost to locations
    )
}