package streetlight.web.layouts

import kotlinx.html.FlowContent
import streetlight.model.data.LocationPost

fun FlowContent.largeLocationPostCard(post: LocationPost) {
    val location = post.location ?: return // td: show removed content

    largePostCard(
        title = post.label,
        subtitle = location.address,
        description = location.description,
        links = null, // td: add location extra links
        image = location.image,
        postRoute = location.route,
        subRoute = null,
        cells = listOf() // td: add cost to locations
    )
}