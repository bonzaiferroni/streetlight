package streetlight.web.layouts

import koala.css.OverflowHidden
import koala.css.Padding0
import koala.css.QueryContainer
import koala.css.modify
import koala.html.card
import kotlinx.html.FlowContent
import streetlight.model.data.LocationPost

fun FlowContent.largeLocationPostCard(post: LocationPost) {
    val location = post.location ?: return // td: show removed content
    val postRoute = location.route

    card(modify(QueryContainer, Padding0, OverflowHidden)) {

    }
}