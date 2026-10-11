package streetlight.web.layout

import kotlinx.html.FlowContent
import streetlight.model.data.LocationPost

fun FlowContent.largeLocationPostCard(post: LocationPost) {
    largePostCard(post)
}