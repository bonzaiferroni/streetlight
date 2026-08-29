package streetlight.model.data

import koala.model.RouteContent
import kotlinx.serialization.Serializable

@Serializable
data class InboxContent(
    val chats: List<ChatPreview>
): RouteContent {
}