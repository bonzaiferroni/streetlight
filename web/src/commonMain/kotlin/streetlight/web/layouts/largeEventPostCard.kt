package streetlight.web.layouts

import koala.html.Attribute
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.EventPost
import streetlight.model.data.GalaxyId

fun FlowContent.largeEventPostCard(post: EventPost) {
    val event = post.event ?: return // td: show removed post content
    val location = post.location ?: return // td: show removed post content

    largePostCard(
        title = post.title,
        subtitle = "${location.name}, ${location.city}",
        description = post.description,
        sourceUrl = event.url,
        links = event.links,
        imageUrl = post.imageUrl,
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

