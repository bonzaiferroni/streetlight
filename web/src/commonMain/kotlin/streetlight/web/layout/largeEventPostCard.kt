package streetlight.web.layout

import kotlinx.html.FlowContent
import streetlight.model.data.EventPost

fun FlowContent.largeEventPostCard(post: EventPost) {
    largePostCard(post)
}

