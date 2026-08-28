package streetlight.model.data

import koala.model.RouteContent
import kotlinx.serialization.Serializable

@Serializable
data class InboxContent(
    val messages: List<Message>
): RouteContent {
}