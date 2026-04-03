package streetlight.web.layouts

import kabinet.utils.toRelativeDayFormat
import koala.SvgFile
import koala.css.*
import koala.html.Attribute
import koala.html.action
import koala.html.actionIfNotNull
import koala.html.btn
import koala.html.card
import koala.html.column
import koala.html.heading3
import koala.html.icon
import koala.html.fillImage
import koala.html.row
import koala.html.setData
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.EventId
import streetlight.model.data.EventPost
import streetlight.model.data.StarType

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
            { starCell(event.eventId) },
        )
    )
}

object EventKey {
    val EventStarId = Attribute<EventId>("event-star-id")
    // val StarClass = Css("event-star")
}

val StarType?.iconPath get() = when(this) {
    StarType.Star -> SvgFile.StarFilled
    StarType.Calendar -> SvgFile.StarFilled // td: handle differently
    null -> SvgFile.StarOutline
}