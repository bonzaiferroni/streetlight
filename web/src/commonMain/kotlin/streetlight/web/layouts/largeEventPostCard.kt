package streetlight.web.layouts

import kotlinx.html.FlowContent
import streetlight.model.data.EventPost

fun FlowContent.largeEventPostCard(post: EventPost) {
    val event = post.event ?: return // td: show removed post content

    largePostCard(
        title = post.title,
        subtitle = "${event.locationName}, ${event.city}",
        description = post.description,
        links = event.links,
        images = post.images,
        postRoute = event.eventRoute,
        subRoute = event.locationRoute,
        cells = listOf(
            { startsAtCell(event.startsAt) },
            { costCell(event.cost, event.url) },
            { postedByCell(post.username) },
            { eventLightCell(event.lightCount, event.eventId) },
        )
    )
}

