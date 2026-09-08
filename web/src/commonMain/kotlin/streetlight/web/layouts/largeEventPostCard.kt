package streetlight.web.layouts

import kotlinx.html.FlowContent
import streetlight.model.data.EventPost
import streetlight.web.ui.starLightCell

fun FlowContent.largeEventPostCard(post: EventPost) {
    val event = post.event ?: return // td: show removed post content

    largePostCard(
        title = post.label,
        subtitle = "${event.locationName}, ${event.city}",
        description = post.body,
        links = event.links,
        image = post.image,
        postRoute = event.eventRoute,
        subRoute = event.locationRoute,
        cells = listOf(
            { startsAtCell(event.startsAt) },
            event.cost?.let {
                { costCell(it, event.url) }
            },
            { starCell(post.post.username) },
            { starLightCell(event) },
        )
    )
}

