package streetlight.web.layouts

import kampfire.model.toUrl
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
            { costCell(event.cost, event.url) },
            { starCell(post.base.username) },
            { starLightCell(event) },
        )
    )
}

