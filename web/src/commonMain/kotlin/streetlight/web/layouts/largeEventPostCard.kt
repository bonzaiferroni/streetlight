package streetlight.web.layouts

import kotlinx.html.FlowContent
import streetlight.model.data.EventPost

fun FlowContent.largeEventPostCard(post: EventPost) {
    val event = post.event ?: return // td: show removed post content
    val location = post.location ?: return // td: show removed post content

    largePostCard(
        title = post.title,
        subtitle = "${location.name}, ${location.city}",
        description = post.description,
        sourceUrl = event.url,
        links = event.links,
        images = post.images,
        postRoute = event.route,
        subRoute = location.route,
        cells = listOf(
            { startsAtCell(event.startsAt) },
            { costCell(event.cost, event.url) },
            { postedBy(post.username) },
            { lightCell(event.eventId) },
        )
    )
}

